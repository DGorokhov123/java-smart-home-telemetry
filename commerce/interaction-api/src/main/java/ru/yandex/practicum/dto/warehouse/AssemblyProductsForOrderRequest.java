package ru.yandex.practicum.dto.warehouse;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class AssemblyProductsForOrderRequest {

    @NotBlank
    private String orderId;

    @NotNull
    @NotEmpty
    private Map<String, Long> products;

}
