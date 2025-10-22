package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.yandex.practicum.client.OrderClient;
import ru.yandex.practicum.client.WarehouseClient;
import ru.yandex.practicum.dal.*;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.delivery.DeliveryState;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.exception.delivery.NoDeliveryFoundException;
import ru.yandex.practicum.logging.Logging;
import ru.yandex.practicum.poller.DeliveryDeliveredPoller;
import ru.yandex.practicum.poller.DeliveryFailedPoller;
import ru.yandex.practicum.poller.DeliveryStartingWarehousePoller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final TransactionTemplate transactionTemplate;
    private final FromAddressRepository fromAddressRepository;
    private final ToAddressRepository toAddressRepository;
    private final DeliveryRepository deliveryRepository;
    private final DeliverySettingsRepository settingsRepository;

    private final DeliveryStartingWarehousePoller deliveryStartingWarehousePoller;
    private final DeliveryDeliveredPoller deliveryDeliveredPoller;
    private final DeliveryFailedPoller deliveryFailedPoller;

    private final WarehouseClient warehouseClient;
    private final OrderClient orderClient;

    // Создать новую доставку в БД.
    // вызывается из сервиса order после бронирования, но перед калькуляцией и созданием платежа
    // проверяем наличие в базе адреса доставки, создаем сущность Delivery, но не запускаем в работу
    // (по идее, создавать доставку можно было бы после успешного платежа, но из-за структуры OrderDTO
    // мы никак не сможем передать адрес доставки в метод калькуляции стоимости доставки кроме как через сущность)
    // поэтому создаем доставку перед платежом, а запускаем после
    @Logging
    @Transactional(readOnly = false)
    public DeliveryDto createNewDelivery(DeliveryDto deliveryDto) {
        Optional<Delivery> optionalDelivery = deliveryRepository.findByOrderId(deliveryDto.getOrderId());
        if (optionalDelivery.isPresent()) return optionalDelivery.get().toDto();    // idempotency

        FromAddress fromAddress = fromAddressRepository.findByAddressDto(deliveryDto.getFromAddress());
        if (fromAddress == null) {
            fromAddress = FromAddress.newEntityFromDto(deliveryDto.getFromAddress());
            fromAddressRepository.save(fromAddress);
        }
        ToAddress toAddress = toAddressRepository.findByAddressDto(deliveryDto.getToAddress());
        if (toAddress == null) {
            toAddress = ToAddress.newEntityFromDto(deliveryDto.getToAddress());
            toAddressRepository.save(toAddress);
        }

        Delivery delivery = new Delivery();
        delivery.setFromAddress(fromAddress);
        delivery.setToAddress(toAddress);
        delivery.setOrderId(deliveryDto.getOrderId());
        delivery.setDeliveryState(DeliveryState.CREATED);
        delivery.setCreatedAt(Instant.now());
        delivery.setModifiedAt(Instant.now());
        delivery.setTouchedAt(Instant.now());
        deliveryRepository.save(delivery);
        return delivery.toDto();
    }

    // Запуск процесса доставки
    // вызывается из order service после успешной оплаты товара. запускает процесс доставки.
    // в этом статусе заказ попадает в ручную обработку службы доставки, которая должна принять товары со
    // склада и сообщить об этом вручную вызвав pickedProductsToDelivery
    @Transactional(readOnly = false)
    public void startDelivery(String orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId).orElseThrow(
                () -> new NoDeliveryFoundException("Not found delivery for orderId = " + orderId)
        );
        delivery.setDeliveryState(DeliveryState.STARTING);
        deliveryRepository.save(delivery);
    }

    // Эмуляция получения товара в доставку.
    // вызывается вручную службой доставки после получения продукта со склада
    // доставка проходит через 2 поллера: один оповещает warehouse, второй order service
    // и останавливается в статусе IN_PROGRESS
    public void pickedProductsToDelivery(String orderId) {
        transactionTemplate.execute(status -> {
            Delivery delivery = deliveryRepository.findByOrderId(orderId).orElseThrow(
                    () -> new NoDeliveryFoundException("Not found delivery for orderId = " + orderId)
            );
            delivery.setDeliveryState(DeliveryState.STARTING_WAREHOUSE);
            deliveryRepository.save(delivery);
            return null;
        });
        deliveryStartingWarehousePoller.touch();
    }

    // Эмуляция успешной доставки товара.
    // вызывается вручную службой доставки после успешной доставки
    // запускает поллер, который оповещает сервис order
    public void successfulDeliveryForOrder(String orderId) {
        transactionTemplate.execute(status -> {
            Delivery delivery = deliveryRepository.findByOrderId(orderId).orElseThrow(
                    () -> new NoDeliveryFoundException("Not found delivery for orderId = " + orderId)
            );
            delivery.setDeliveryState(DeliveryState.DELIVERED);
            deliveryRepository.save(delivery);
            return null;
        });
        deliveryDeliveredPoller.touch();
    }

    // Эмуляция неудачного вручения товара.
    // вызывается вручную службой доставки в случае отмены доставки
    // запускает поллер, который оповещает сервис order
    public void failedDeliveryForOrder(String orderId) {
        transactionTemplate.execute(status -> {
            Delivery delivery = deliveryRepository.findByOrderId(orderId).orElseThrow(
                    () -> new NoDeliveryFoundException("Not found delivery for orderId = " + orderId)
            );
            delivery.setDeliveryState(DeliveryState.FAILED);
            deliveryRepository.save(delivery);
            return null;
        });
        deliveryFailedPoller.touch();
    }

    // Расчёт полной стоимости доставки заказа.
    // вызывается из сервиса order на этапе расчета стоимости доставки
    @Logging
    public BigDecimal calculateDeliveryCost(OrderDto orderDto) {
        DeliverySettings settings = settingsRepository.findFirstByOrderByIdDesc().orElseThrow(
                () -> new RuntimeException("Not found delivery settings in database")
        );
        Delivery delivery = deliveryRepository.findById(orderDto.getDeliveryId()).orElseThrow(
                () -> new NoDeliveryFoundException("Not found delivery " + orderDto.getDeliveryId())
        );
        // calculations:
        BigDecimal cost = settings.getBaseCost();
        // warehouse-based multiplicator
        cost = delivery.getFromAddress().getPriceMultiplicator().add(BigDecimal.ONE).multiply(cost);
        // fragility-based multiplicator
        if (orderDto.getFragile() != null && orderDto.getFragile()) {
            cost = settings.getFragilityMultiplicator().add(BigDecimal.ONE).multiply(cost);
        }
        // weight-based multiplicator
        if (orderDto.getDeliveryWeight() != null) {
            cost = settings.getWeightMultiplicator().multiply(orderDto.getDeliveryWeight()).add(cost);
        }
        // volume-based multiplicator
        if (orderDto.getDeliveryVolume() != null) {
            cost = settings.getVolumeMultiplicator().multiply(orderDto.getDeliveryVolume()).add(cost);
        }
        // street-based multiplicator
        if (
                !Objects.equals(delivery.getFromAddress().getCountry(), delivery.getToAddress().getCountry())
                        || !Objects.equals(delivery.getFromAddress().getCity(), delivery.getToAddress().getCity())
                        || !Objects.equals(delivery.getFromAddress().getStreet(), delivery.getToAddress().getStreet())
        ) {
            cost = settings.getStreetMultiplicator().add(BigDecimal.ONE).multiply(cost);
        }
        // set scale and return
        return cost.setScale(2, RoundingMode.UP);
    }

}
