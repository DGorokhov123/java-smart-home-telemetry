package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.ShoppingStoreApi;
import ru.yandex.practicum.dto.store.ProductCategory;
import ru.yandex.practicum.dto.store.ProductCollectionDto;
import ru.yandex.practicum.dto.store.ProductDto;
import ru.yandex.practicum.dto.store.QuantityState;
import ru.yandex.practicum.service.ProductService;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Validated
@RestController
@RequiredArgsConstructor
public class ShoppingStoreController implements ShoppingStoreApi {

    private final ProductService productService;

    // Создание нового товара в ассортименте
    @Override
    public ProductDto createProduct(ProductDto productDto) {
        return productService.createProduct(productDto);
    }

    // Обновление товара в ассортименте, например уточнение описания, характеристик и т.д.
    @Override
    public ProductDto updateProduct(ProductDto productDto) {
        return productService.updateProduct(productDto);
    }

    // Удалить товар из ассортимента магазина. Функция для менеджерского состава.
    @Override
    public String removeProduct(String productId) {
        productId = productId.replaceAll("\"", "");       // remove quotes
        return productService.removeProduct(productId);
    }

    // Установка статуса по товару. API вызывается со стороны склада.
    @Override
    public String setQuantityState(String productId, QuantityState quantityState) {
        return productService.setQuantityState(productId, quantityState);
    }

    // Получить сведения по товару из БД.
    @Override
    public ProductDto getById(String productId) {
        return productService.getById(productId);
    }

    // Получить цены по списку товаров из БД
    @Override
    public Map<String, BigDecimal> getPricesByIds(Set<String> productIds) {
        return productService.getPricesByIds(productIds);
    }

    // Получение списка товаров по типу в пагинированном виде
    @Override
    public ProductCollectionDto getCollection(ProductCategory category, Integer page, Integer size, String sort) {
        return productService.getCollection(category, page, size, sort);
    }

}
