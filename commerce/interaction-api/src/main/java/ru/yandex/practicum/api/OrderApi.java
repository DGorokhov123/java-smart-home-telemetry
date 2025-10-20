package ru.yandex.practicum.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.order.ProductReturnRequest;
import ru.yandex.practicum.validation.ValidUsername;

import java.util.List;

public interface OrderApi {

    // Получить заказы пользователя.
    @GetMapping("/api/v1/order")
    @ResponseStatus(HttpStatus.OK)
    List<OrderDto> getOrdersByUsername(
            @RequestParam(required = true) @ValidUsername String username
    );

    // Создать новый заказ в системе.
    @PutMapping("/api/v1/order")
    @ResponseStatus(HttpStatus.OK)
    OrderDto createNewOrder(
            @RequestParam(required = true) @ValidUsername String username,
            @RequestBody @Valid CreateNewOrderRequest createNewOrderRequest
    );

    // Возврат заказа.
    @PostMapping("/api/v1/order/return")
    @ResponseStatus(HttpStatus.OK)
    OrderDto returnProductsInOrder(
            @RequestBody @NotBlank String orderId
    );

    // Оплата заказа.
    @PostMapping("/api/v1/order/payment")
    @ResponseStatus(HttpStatus.OK)
    OrderDto successfulPaymentForOrderId(
            @RequestBody @NotBlank String orderId
    );

    // Оплата заказа произошла с ошибкой.
    @PostMapping("/api/v1/order/payment/failed")
    @ResponseStatus(HttpStatus.OK)
    OrderDto failedPaymentForOrderId(
            @RequestBody @NotBlank String orderId
    );

    // Доставка заказа стартовала
    @PostMapping("/api/v1/order/delivery/started")
    @ResponseStatus(HttpStatus.OK)
    OrderDto startedOrderDelivery(
            @RequestBody @NotBlank String orderId
    );

    // Доставка заказа завершена успешно
    @PostMapping("/api/v1/order/delivery")
    @ResponseStatus(HttpStatus.OK)
    OrderDto successfulOrderDelivery(
            @RequestBody @NotBlank String orderId
    );

    // Доставка заказа произошла с ошибкой.
    @PostMapping("/api/v1/order/delivery/failed")
    @ResponseStatus(HttpStatus.OK)
    OrderDto failedOrderDelivery(
            @RequestBody @NotBlank String orderId
    );

    // Завершение заказа.
    @PostMapping("/api/v1/order/completed")
    @ResponseStatus(HttpStatus.OK)
    OrderDto completedOrder(
            @RequestBody @NotBlank String orderId
    );

    // Расчёт стоимости заказа.
    @PostMapping("/api/v1/order/calculate/total")
    @ResponseStatus(HttpStatus.OK)
    OrderDto calculateTotalPriceForOrder(
            @RequestBody @NotBlank String orderId
    );

    // Расчёт стоимости доставки заказа.
    @PostMapping("/api/v1/order/calculate/delivery")
    @ResponseStatus(HttpStatus.OK)
    OrderDto calculateDeliveryPriceForOrder(
            @RequestBody @NotBlank String orderId
    );

    // Сборка заказа.
    @PostMapping("/api/v1/order/assembly")
    @ResponseStatus(HttpStatus.OK)
    OrderDto successfulOrderAssembly(
            @RequestBody @NotBlank String orderId
    );

    // Сборка заказа произошла с ошибкой.
    @PostMapping("/api/v1/order/assembly/failed")
    @ResponseStatus(HttpStatus.OK)
    OrderDto failedOrderAssembly(
            @RequestBody @NotBlank String orderId
    );

}
