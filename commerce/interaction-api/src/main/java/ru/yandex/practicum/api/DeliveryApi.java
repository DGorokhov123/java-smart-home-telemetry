package ru.yandex.practicum.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.order.OrderDto;

import java.math.BigDecimal;

public interface DeliveryApi {

    // Создать новую доставку в БД.
    @PutMapping("/api/v1/delivery")
    @ResponseStatus(HttpStatus.OK)
    DeliveryDto createNewDelivery(
            @RequestBody @Valid DeliveryDto deliveryDto
    );

    // Эмуляция успешной доставки товара.
    @PostMapping("/api/v1/delivery/successful")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void successfulDeliveryForOrder(
            @RequestBody @NotBlank String orderId
    );

    // Запуск процесса доставки
    @PostMapping("/api/v1/delivery/start")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void startDelivery(
            @RequestBody @NotBlank String orderId
    );

    // Эмуляция получения товара в доставку.
    @PostMapping("/api/v1/delivery/picked")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void pickedProductsToDelivery(
            @RequestBody @NotBlank String orderId
    );

    // Эмуляция неудачного вручения товара.
    @PostMapping("/api/v1/delivery/failed")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void failedDeliveryForOrder(
            @RequestBody @NotBlank String orderId
    );

    // Расчёт полной стоимости доставки заказа.
    @PostMapping("/api/v1/delivery/cost")
    @ResponseStatus(HttpStatus.OK)
    BigDecimal calculateDeliveryCost(
            @RequestBody @Valid OrderDto orderDto
    );

}
