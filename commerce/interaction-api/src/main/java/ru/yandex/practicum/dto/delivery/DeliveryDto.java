package ru.yandex.practicum.dto.delivery;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.validation.NotBlankButNullAllowed;

@Data
public class DeliveryDto {

    @NotBlankButNullAllowed
    private String deliveryId;

    @NotNull
    private AddressDto fromAddress;

    @NotNull
    private AddressDto toAddress;

    @NotBlank
    private String orderId;

    private DeliveryState deliveryState;

}
