package ru.yandex.practicum.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.payment.PaymentDto;

import java.math.BigDecimal;

public interface PaymentApi {

    // Формирование оплаты для заказа (переход в платежный шлюз).
    @PostMapping("/api/v1/payment")
    @ResponseStatus(HttpStatus.OK)
    PaymentDto makePaymentForOrder(
            @RequestBody @Valid OrderDto orderDto
    );

    // Расчёт полной стоимости заказа.
    @PostMapping("/api/v1/payment/totalCost")
    @ResponseStatus(HttpStatus.OK)
    BigDecimal calculateTotalCostForOrder(
            @RequestBody @Valid OrderDto orderDto
    );

    // Метод для эмуляции успешной оплаты в платежного шлюза.
    @PostMapping("/api/v1/payment/success")
    @ResponseStatus(HttpStatus.OK)
    void successfulPaymentForOrder(
            @RequestBody @NotBlank String orderId
    );

    // Расчёт стоимости товаров в заказе.
    @PostMapping("/api/v1/payment/productCost")
    @ResponseStatus(HttpStatus.OK)
    BigDecimal calculateProductCostForOrder(
            @RequestBody @Valid OrderDto orderDto
    );

    // Метод для эмуляции отказа в оплате платежного шлюза.
    @PostMapping("/api/v1/payment/failed")
    @ResponseStatus(HttpStatus.OK)
    void failedPaymentForOrder(
            @RequestBody @NotBlank String orderId
    );

}
