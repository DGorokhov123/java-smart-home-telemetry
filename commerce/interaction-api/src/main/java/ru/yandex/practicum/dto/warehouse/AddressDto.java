package ru.yandex.practicum.dto.warehouse;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record AddressDto (

    @NotBlank
    String country,

    @NotBlank
    String city,

    @NotBlank
    String street,

    @NotBlank
    String house,

    @NotNull
    String flat

) {}
