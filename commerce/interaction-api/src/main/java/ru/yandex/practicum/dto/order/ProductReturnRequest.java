package ru.yandex.practicum.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class ProductReturnRequest {

    @NotBlank
    private String orderId;

    @NotNull
    @NotEmpty
    private Map<String, Long> products;

}
