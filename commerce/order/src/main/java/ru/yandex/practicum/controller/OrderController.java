package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.OrderApi;
import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.order.ProductReturnRequest;
import ru.yandex.practicum.service.OrderService;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
public class OrderController implements OrderApi {

    private final OrderService orderService;

    // Получить заказы пользователя.
    @Override
    public List<OrderDto> getOrdersByUsername(String username) {
        return orderService.getOrdersByUsername(username);
    }

    // Создать новый заказ в системе.
    @Override
    public OrderDto createNewOrder(String username, CreateNewOrderRequest createNewOrderRequest) {
        return orderService.createNewOrder(username, createNewOrderRequest);
    }

    // Возврат заказа.
    @Override
    public OrderDto returnProductsInOrder(String orderId) {
        orderId = orderId.replaceAll("\"", "");       // remove quotes
        return orderService.returnProductsInOrder(orderId);
    }

    // Оплата заказа.
    @Override
    public OrderDto successfulPaymentForOrderId(String orderId) {
        orderId = orderId.replaceAll("\"", "");       // remove quotes
        return orderService.successfulPaymentForOrderId(orderId);
    }

    // Оплата заказа произошла с ошибкой.
    @Override
    public OrderDto failedPaymentForOrderId(String orderId) {
        orderId = orderId.replaceAll("\"", "");       // remove quotes
        return orderService.failedPaymentForOrderId(orderId);
    }

    // Доставка заказа стартовала
    @Override
    public OrderDto startedOrderDelivery(String orderId) {
        orderId = orderId.replaceAll("\"", "");       // remove quotes
        return orderService.startedOrderDelivery(orderId);
    }

    // Доставка заказа завершена успешно
    @Override
    public OrderDto successfulOrderDelivery(String orderId) {
        orderId = orderId.replaceAll("\"", "");       // remove quotes
        return orderService.successfulOrderDelivery(orderId);
    }

    // Доставка заказа произошла с ошибкой.
    @Override
    public OrderDto failedOrderDelivery(String orderId) {
        orderId = orderId.replaceAll("\"", "");       // remove quotes
        return orderService.failedOrderDelivery(orderId);
    }

    // Завершение заказа.
    @Override
    public OrderDto completedOrder(String orderId) {
        orderId = orderId.replaceAll("\"", "");       // remove quotes
        return orderService.completedOrder(orderId);
    }

    // Расчёт стоимости заказа.
    @Override
    public OrderDto calculateTotalPriceForOrder(String orderId) {
        orderId = orderId.replaceAll("\"", "");       // remove quotes
        return orderService.calculateTotalPriceForOrder(orderId);
    }

    // Расчёт стоимости доставки заказа.
    @Override
    public OrderDto calculateDeliveryPriceForOrder(String orderId) {
        orderId = orderId.replaceAll("\"", "");       // remove quotes
        return orderService.calculateDeliveryPriceForOrder(orderId);
    }

    // Сборка заказа.
    @Override
    public OrderDto successfulOrderAssembly(String orderId) {
        orderId = orderId.replaceAll("\"", "");       // remove quotes
        return orderService.successfulOrderAssembly(orderId);
    }

    // Сборка заказа произошла с ошибкой.
    @Override
    public OrderDto failedOrderAssembly(String orderId) {
        orderId = orderId.replaceAll("\"", "");       // remove quotes
        return orderService.failedOrderAssembly(orderId);
    }

}
