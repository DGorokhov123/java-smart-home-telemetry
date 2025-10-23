package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import ru.yandex.practicum.client.OrderClient;
import ru.yandex.practicum.client.ShoppingStoreClient;
import ru.yandex.practicum.dal.Payment;
import ru.yandex.practicum.dal.PaymentRepository;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.dto.payment.PaymentState;
import ru.yandex.practicum.exception.payment.NoPaymentFoundException;
import ru.yandex.practicum.exception.payment.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.poller.PaymentStateFailPoller;
import ru.yandex.practicum.poller.PaymentStateSuccessPoller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TransactionTemplate transactionTemplate;
    private final PaymentRepository paymentRepository;

    private final PaymentStateSuccessPoller paymentStateSuccessPoller;
    private final PaymentStateFailPoller paymentStateFailPoller;

    private final ShoppingStoreClient shoppingStoreClient;
    private final OrderClient orderClient;

    // Формирование оплаты для заказа (переход в платежный шлюз).
    // просто создаем новый Payment и гипотетически пересылаем данные в платежную систему
    public PaymentDto makePaymentForOrder(OrderDto orderDto) {
        PaymentDto paymentDto = transactionTemplate.execute(status -> {
            Optional<Payment> optionalPayment = paymentRepository.findByOrderId(orderDto.getOrderId());
            if (optionalPayment.isPresent()) return optionalPayment.get().toDto();         // idempotency

            Payment payment = new Payment();
            payment.setOrderId(orderDto.getOrderId());
            payment.setTotalPayment(orderDto.getTotalPrice());
            payment.setDeliveryTotal(orderDto.getDeliveryPrice());
            payment.setFeeTotal(orderDto.getTotalPrice().multiply(new BigDecimal("0.1")));
            payment.setPaymentState(PaymentState.PENDING);
            payment.setCreatedAt(Instant.now());
            payment.setModifiedAt(Instant.now());
            payment.setTouchedAt(Instant.now());
            paymentRepository.save(payment);
            return payment.toDto();
        });
        // paymentStatePendingPoller.touch();
        // тут мы могли бы послать касание поллеру, который должен пересылать данные в платежную систему
        // но у нас его нет, как и платежной системы, поэтому просто учтем этот факт
        return paymentDto;
    }

    // Расчёт стоимости товаров в заказе.
    // метод не меняет состояние, поэтому feign запрос не нужно вызывать через poller
    // вылетает по FeignException, если не.
    // лучше бы перенести его в сервис order, но по спецификации он зачем-то тут
    public BigDecimal calculateProductCostForOrder(OrderDto orderDto) {
        Map<String, BigDecimal> prices = shoppingStoreClient.getPricesByIds(orderDto.getProducts().keySet());
        if (prices.size() < orderDto.getProducts().size()) {
            String errorMsg = orderDto.getProducts().keySet().stream()
                    .filter(id -> !prices.containsKey(id))
                    .map(id -> id + ", ")
                    .reduce(new StringBuilder("Prices not found for: "), StringBuilder::append, StringBuilder::append)
                    .toString();
            throw new NotEnoughInfoInOrderToCalculateException(errorMsg);
        }
        return orderDto.getProducts().entrySet().stream()
                .map(e -> BigDecimal.valueOf(e.getValue()).multiply(prices.get(e.getKey())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.UP);
    }

    // Расчёт полной стоимости заказа.
    // помимо очевидного сложения считает налоги, что в общем случае может быть довольно сложно
    public BigDecimal calculateTotalCostForOrder(OrderDto orderDto) {
        BigDecimal fee = orderDto.getProductPrice().multiply(new BigDecimal("0.1"));
        BigDecimal productsCostWithFee = orderDto.getProductPrice().add(fee);
        BigDecimal costWithFeeAndDelivery = productsCostWithFee.add(orderDto.getDeliveryPrice());
        return costWithFeeAndDelivery.setScale(2, RoundingMode.UP);
    }

    // Метод для эмуляции успешной оплаты в платежного шлюза.
    // (предполагается, что когда платежная система получит платеж, она вызовет этот метод)
    // просто меняем статус Payment и дергаем поллер чтобы он переслал данные в другие сервисы
    public void successfulPaymentForOrder(String orderId) {
        transactionTemplate.execute(status -> {
            Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow(
                    () -> new NoPaymentFoundException("Not found payment for order " + orderId)
            );
            payment.setPaymentState(PaymentState.SUCCESS);
            paymentRepository.save(payment);
            return null;
        });
        paymentStateSuccessPoller.touch();
    }


    // Метод для эмуляции отказа в оплате платежного шлюза.
    // (предполагается, что когда платежная система получит отказ, она вызовет этот метод)
    // аналог предыдущего, но с другим статусом
    public void failedPaymentForOrder(String orderId) {
        transactionTemplate.execute(status -> {
            Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow(
                    () -> new NoPaymentFoundException("Not found payment for order " + orderId)
            );
            payment.setPaymentState(PaymentState.FAIL);
            paymentRepository.save(payment);
            return null;
        });
        paymentStateFailPoller.touch();
    }

}
