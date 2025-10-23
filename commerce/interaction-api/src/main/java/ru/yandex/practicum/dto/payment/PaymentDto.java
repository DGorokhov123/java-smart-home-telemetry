package ru.yandex.practicum.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentDto {

    @NotBlank
    private String paymentId;

    @NotNull
    private BigDecimal totalPayment;

    @NotNull
    private BigDecimal deliveryTotal;

    @NotNull
    private BigDecimal feeTotal;

}
