package ru.yandex.practicum.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.store.ProductCategory;
import ru.yandex.practicum.dto.store.ProductCollectionDto;
import ru.yandex.practicum.dto.store.ProductDto;
import ru.yandex.practicum.dto.store.QuantityState;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ShoppingStoreApi {

    // Создание нового товара в ассортименте
    @PutMapping("/api/v1/shopping-store")
    @ResponseStatus(HttpStatus.OK)
    public ProductDto createProduct(
            @RequestBody @Valid ProductDto productDto
    );

    // Обновление товара в ассортименте, например уточнение описания, характеристик и т.д.
    @PostMapping("/api/v1/shopping-store")
    @ResponseStatus(HttpStatus.OK)
    public ProductDto updateProduct(
            @RequestBody @Valid ProductDto productDto
    );

    // Удалить товар из ассортимента магазина. Функция для менеджерского состава.
    @PostMapping("/api/v1/shopping-store/removeProductFromStore")
    @ResponseStatus(HttpStatus.OK)
    public String removeProduct(
            @RequestBody @NotBlank String productId
    );

    // Установка статуса по товару. API вызывается со стороны склада.
    @PostMapping("/api/v1/shopping-store/quantityState")
    @ResponseStatus(HttpStatus.OK)
    public String setQuantityState(
            @RequestParam(required = true) String productId,
            @RequestParam(required = true) QuantityState quantityState
    );

    // Получить сведения по товару из БД.
    @GetMapping("/api/v1/shopping-store/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public ProductDto getById(
            @PathVariable String productId
    );

    // Получить цены по списку товаров из БД
    @PostMapping("/api/v1/shopping-store/price")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, BigDecimal> getPricesByIds(
            @RequestBody @NotNull @NotEmpty Set<String> productIds
    );

    // Получение списка товаров по типу в пагинированном виде
    @GetMapping("/api/v1/shopping-store")
    @ResponseStatus(HttpStatus.OK)
    public ProductCollectionDto getCollection(
            @RequestParam(required = true) ProductCategory category,
            @RequestParam(required = false, defaultValue = "0") @PositiveOrZero Integer page,
            @RequestParam(required = false, defaultValue = "10") @Positive Integer size,
            @RequestParam(required = false, defaultValue = "productName,ASC") String sort
    );

}
