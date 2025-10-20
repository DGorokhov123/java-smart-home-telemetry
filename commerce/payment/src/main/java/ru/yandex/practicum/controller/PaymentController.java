package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.PaymentApi;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.service.PaymentService;

import java.math.BigDecimal;

@Validated
@RestController
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {

    private final PaymentService paymentService;

    // Формирование оплаты для заказа (переход в платежный шлюз).
    @Override
    public PaymentDto makePaymentForOrder(OrderDto orderDto) {
        return paymentService.makePaymentForOrder(orderDto);
    }

    // Расчёт полной стоимости заказа.
    @Override
    public BigDecimal calculateTotalCostForOrder(OrderDto orderDto) {
        return paymentService.calculateTotalCostForOrder(orderDto);
    }

    // Метод для эмуляции успешной оплаты в платежного шлюза.
    @Override
    public void successfulPaymentForOrder(String orderId) {
        orderId = orderId.replaceAll("\"", "");       // remove quotes
        paymentService.successfulPaymentForOrder(orderId);
    }

    // Расчёт стоимости товаров в заказе.
    @Override
    public BigDecimal calculateProductCostForOrder(OrderDto orderDto) {
        return paymentService.calculateProductCostForOrder(orderDto);
    }

    // Метод для эмуляции отказа в оплате платежного шлюза.
    @Override
    public void failedPaymentForOrder(String orderId) {
        orderId = orderId.replaceAll("\"", "");       // remove quotes
        paymentService.failedPaymentForOrder(orderId);
    }

}
