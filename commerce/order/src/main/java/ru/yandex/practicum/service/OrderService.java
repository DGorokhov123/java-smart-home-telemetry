package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.yandex.practicum.client.DeliveryClient;
import ru.yandex.practicum.client.PaymentClient;
import ru.yandex.practicum.client.ShoppingStoreClient;
import ru.yandex.practicum.client.WarehouseClient;
import ru.yandex.practicum.dal.*;
import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.order.OrderState;
import ru.yandex.practicum.dto.order.ProductReturnRequest;
import ru.yandex.practicum.exception.order.NoOrderFoundException;
import ru.yandex.practicum.poller.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final TransactionTemplate transactionTemplate;
    private final OrderRepository orderRepository;
    private final FromAddressRepository fromAddressRepository;
    private final ToAddressRepository toAddressRepository;

    private final OrderStateNewPoller orderStateNewPoller;
    private final OrderStatePaidPoller orderStatePaidPoller;
    private final OrderStateDonePoller orderStateDonePoller;
    private final OrderStateCalculatedPoller orderStateCalculatedPoller;
    private final OrderStateAssembledPoller orderStateAssembledPoller;
    private final OrderStateReturnProductsPoller orderStateReturnProductsPoller;

    private final PaymentClient paymentClient;
    private final DeliveryClient deliveryClient;
    private final WarehouseClient warehouseClient;
    private final ShoppingStoreClient shoppingStoreClient;

    // Получить заказы пользователя.
    // вызывается клиентом с фронта для просмотра своих заказов
    // ну и в принципе откуда угодно, тк других методов запроса заказа в api не предусмотрено
    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByUsername(String username) {
        return orderRepository.findAllByUsernameOrderByCreatedAtDesc(username).stream()
                .map(Order::toDto)
                .toList();
    }

    // Создать новый заказ в системе.
    // запускается клиентом с фронта при нажатии "Заказать" в корзине
    // создает Order и запускает каскад поллеров для прохождения заказа в оплату
    public OrderDto createNewOrder(String username, CreateNewOrderRequest request) {
        OrderDto orderDto = transactionTemplate.execute(status -> {
            Optional<Order> optionalOrder = orderRepository.findByShoppingCartId(request.getShoppingCart().getShoppingCartId());
            if (optionalOrder.isPresent()) return optionalOrder.get().toDto();       // idempotence

            ToAddress toAddress = toAddressRepository.findByCountryAndCityAndStreetAndHouseAndFlat(
                    request.getDeliveryAddress().getCountry(),
                    request.getDeliveryAddress().getCity(),
                    request.getDeliveryAddress().getStreet(),
                    request.getDeliveryAddress().getHouse(),
                    request.getDeliveryAddress().getFlat()
            );
            if (toAddress == null) {
                toAddress = new ToAddress();
                toAddress.setCountry(request.getDeliveryAddress().getCountry());
                toAddress.setCity(request.getDeliveryAddress().getCity());
                toAddress.setStreet(request.getDeliveryAddress().getStreet());
                toAddress.setHouse(request.getDeliveryAddress().getHouse());
                toAddress.setFlat(request.getDeliveryAddress().getFlat());
                toAddressRepository.save(toAddress);
            }

            Order order = new Order();
            order.setUsername(username);
            order.setShoppingCartId(request.getShoppingCart().getShoppingCartId());
            order.setProducts(request.getShoppingCart().getProducts());
            order.setToAddress(toAddress);
            order.setState(OrderState.NEW);
            order.setCreatedAt(Instant.now());
            order.setModifiedAt(Instant.now());
            order.setTouchedAt(Instant.now());
            orderRepository.save(order);
            return order.toDto();
        });
        orderStateNewPoller.touch();
        return orderDto;
    }

    // Возврат заказа.
    // вызывается вручную службой контроля при неудачной доставке
    // или при отмене заказа до передачи в доставку
    public OrderDto returnProductsInOrder(String orderId) {
        OrderDto orderDto = transactionTemplate.execute(status -> {
            Order order = orderRepository.findById(orderId).orElseThrow(
                    () -> new NoOrderFoundException("Not found order " + orderId)
            );
            order.setState(OrderState.RETURN_PRODUCTS);
            order.setModifiedAt(Instant.now());
            order.setTouchedAt(Instant.now());
            orderRepository.save(order);
            return order.toDto();
        });
        orderStateReturnProductsPoller.touch();
        return orderDto;
    }

    // Оплата заказа.
    // вызывается из сервиса payment при успешной оплате
    // меняет статус на PAID и запускает OrderStatePaidPoller для старта доставки
    public OrderDto successfulPaymentForOrderId(String orderId) {
        OrderDto orderDto = transactionTemplate.execute(status -> {
            Order order = orderRepository.findById(orderId).orElseThrow(
                    () -> new NoOrderFoundException("Not found order " + orderId)
            );
            order.setState(OrderState.PAID);
            order.setModifiedAt(Instant.now());
            order.setTouchedAt(Instant.now());
            orderRepository.save(order);
            return order.toDto();
        });
        orderStatePaidPoller.touch();
        return orderDto;
    }

    // Оплата заказа произошла с ошибкой.
    // вызывается из сервиса payment при неудачном завершении оплаты
    // заказ переходит в статус PAYMENT_FAILED и далее обрабатывается вручную
    @Transactional(readOnly = false)
    public OrderDto failedPaymentForOrderId(String orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new NoOrderFoundException("Not found order " + orderId)
        );
        order.setState(OrderState.PAYMENT_FAILED);
        order.setModifiedAt(Instant.now());
        order.setTouchedAt(Instant.now());
        orderRepository.save(order);
        return order.toDto();
    }

    // Доставка заказа стартовала
    // вызывается из сервиса delivery после получения товара со склада и начала процедуры доставки
    // заказ остается в статусе ON_DELIVERY, ждет информации от службы доставки
    @Transactional(readOnly = false)
    public OrderDto startedOrderDelivery(String orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new NoOrderFoundException("Not found order " + orderId)
        );
        order.setState(OrderState.ON_DELIVERY);
        order.setModifiedAt(Instant.now());
        order.setTouchedAt(Instant.now());
        orderRepository.save(order);
        return order.toDto();
    }

    // Доставка заказа завершена успешно
    // вызывается из сервиса delivery после успешной доставки
    // заказ остается в статусе DELIVERED, далее вручную обрабатывается службой контроля выполнения заказов
    @Transactional(readOnly = false)
    public OrderDto successfulOrderDelivery(String orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new NoOrderFoundException("Not found order " + orderId)
        );
        order.setState(OrderState.DELIVERED);
        order.setModifiedAt(Instant.now());
        order.setTouchedAt(Instant.now());
        orderRepository.save(order);
        return order.toDto();
    }

    // Доставка заказа произошла с ошибкой.
    // вызывается из сервиса delivery в случае неудачной доставки.
    // заказ остается в статусе DELIVERY_FAILED, далее идет в ручную обработку
    @Transactional(readOnly = false)
    public OrderDto failedOrderDelivery(String orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new NoOrderFoundException("Not found order " + orderId)
        );
        order.setState(OrderState.DELIVERY_FAILED);
        order.setModifiedAt(Instant.now());
        order.setTouchedAt(Instant.now());
        orderRepository.save(order);
        return order.toDto();
    }

    // Завершение заказа.
    // вызывается вручную службой контроля выполнения заказов после подтверждения от клиента, что все ок
    // переведит заявку в DONE и запускает поллер, который спишет товар со склада и закроет заявку
    public OrderDto completedOrder(String orderId) {
        OrderDto orderDto = transactionTemplate.execute(status -> {
            Order order = orderRepository.findById(orderId).orElseThrow(
                    () -> new NoOrderFoundException("Not found order " + orderId)
            );
            order.setState(OrderState.DONE);
            order.setModifiedAt(Instant.now());
            order.setTouchedAt(Instant.now());
            orderRepository.save(order);
            return order.toDto();
        });
        orderStateDonePoller.touch();
        return orderDto;
    }

    // Расчёт стоимости заказа.
    // повторяет такой же метод из сервиса payment. я решил что он будет там, а не тут. почему?
    // метод считает налоги. и если сейчас это просто умножение на 1.1 то в общем случае это может быть более сложная формула,
    // требующая доступа к базе с налогами. и подобная функциональность уместнее в сервисе payment
    // поэтому этот метод будет возвращать заказ на шаг CALCULATED для принудительного пересчета стоимости
    public OrderDto calculateTotalPriceForOrder(String orderId) {
        OrderDto orderDto = transactionTemplate.execute(status -> {
            Order order = orderRepository.findById(orderId).orElseThrow(
                    () -> new NoOrderFoundException("Not found order " + orderId)
            );
            order.setState(OrderState.CALCULATED);
            order.setModifiedAt(Instant.now());
            order.setTouchedAt(Instant.now());
            orderRepository.save(order);
            return order.toDto();
        });
        orderStateCalculatedPoller.touch();
        return orderDto;
    }

    // Расчёт стоимости доставки заказа.
    // повторяет такой же метод из сервиса delivery. там он явно уместнее, поскольку там есть информация для расчета
    // поэтому этот метод будет возвращать заказ на шаг ASSEMBLED для принудительного пересчета доставки
    public OrderDto calculateDeliveryPriceForOrder(String orderId) {
        OrderDto orderDto = transactionTemplate.execute(status -> {
            Order order = orderRepository.findById(orderId).orElseThrow(
                    () -> new NoOrderFoundException("Not found order " + orderId)
            );
            order.setState(OrderState.ASSEMBLED);
            order.setModifiedAt(Instant.now());
            order.setTouchedAt(Instant.now());
            orderRepository.save(order);
            return order.toDto();
        });
        orderStateAssembledPoller.touch();
        return orderDto;
    }

    // Сборка заказа.
    // вызывается вручную в случае решения проблем со сборкой заказа.
    // при нормальном прохождении заказа не требуется тк бронирование автоматическое
    @Transactional(readOnly = false)
    public OrderDto successfulOrderAssembly(String orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new NoOrderFoundException("Not found order " + orderId)
        );
        order.setState(OrderState.ASSEMBLED);
        order.setModifiedAt(Instant.now());
        order.setTouchedAt(Instant.now());
        orderRepository.save(order);
        return order.toDto();
    }

    // Сборка заказа произошла с ошибкой.
    // вызывается вручную в случае каких-либо проблем со сборкой заказа. теоретически может возникнуть на этапе
    // ON_PAYMENT или SENT_TO_DELIVERY.  Заказ попадает в ручную обработку.
    @Transactional(readOnly = false)
    public OrderDto failedOrderAssembly(String orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new NoOrderFoundException("Not found order " + orderId)
        );
        order.setState(OrderState.ASSEMBLY_FAILED);
        order.setModifiedAt(Instant.now());
        order.setTouchedAt(Instant.now());
        orderRepository.save(order);
        return order.toDto();
    }

}
