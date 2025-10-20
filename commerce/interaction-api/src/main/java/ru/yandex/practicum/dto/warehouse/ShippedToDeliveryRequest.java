package ru.yandex.practicum.dto.warehouse;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ShippedToDeliveryRequest {

    @NotBlank
    private String orderId;

    @NotBlank
    private String deliveryId;

}
