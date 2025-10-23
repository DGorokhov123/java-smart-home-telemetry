package ru.yandex.practicum.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class OrderDto {

    @NotBlank
    private String orderId;

    @NotBlank
    private String shoppingCartId;

    @NotNull
    @NotEmpty
    private Map<String, Long> products;

    private String paymentId;

    private String deliveryId;

    @NotNull
    private OrderState state;

    private BigDecimal deliveryWeight;

    private BigDecimal deliveryVolume;

    private Boolean fragile;

    private BigDecimal totalPrice;

    private BigDecimal deliveryPrice;

    private BigDecimal productPrice;

}
