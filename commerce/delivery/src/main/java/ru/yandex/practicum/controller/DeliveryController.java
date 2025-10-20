package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.DeliveryApi;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.service.DeliveryService;

import java.math.BigDecimal;

@Validated
@RestController
@RequiredArgsConstructor
public class DeliveryController implements DeliveryApi {

    private final DeliveryService deliveryService;

    // Создать новую доставку в БД.
    @Override
    public DeliveryDto createNewDelivery(DeliveryDto deliveryDto) {
        return deliveryService.createNewDelivery(deliveryDto);
    }

    // Эмуляция успешной доставки товара.
    @Override
    public void successfulDeliveryForOrder(String orderId) {
        orderId = orderId.replaceAll("\"", "");       // remove quotes
        deliveryService.successfulDeliveryForOrder(orderId);
    }

    // Запуск процесса доставки
    @Override
    public void startDelivery(String orderId) {
        orderId = orderId.replaceAll("\"", "");       // remove quotes
        deliveryService.startDelivery(orderId);
    }

    // Эмуляция получения товара в доставку.
    @Override
    public void pickedProductsToDelivery(String orderId) {
        orderId = orderId.replaceAll("\"", "");       // remove quotes
        deliveryService.pickedProductsToDelivery(orderId);
    }

    // Эмуляция неудачного вручения товара.
    @Override
    public void failedDeliveryForOrder(String orderId) {
        orderId = orderId.replaceAll("\"", "");       // remove quotes
        deliveryService.failedDeliveryForOrder(orderId);
    }

    // Расчёт полной стоимости доставки заказа.
    @Override
    public BigDecimal calculateDeliveryCost(OrderDto orderDto) {
        return deliveryService.calculateDeliveryCost(orderDto);
    }

}
