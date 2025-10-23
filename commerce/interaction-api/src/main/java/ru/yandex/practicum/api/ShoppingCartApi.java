package ru.yandex.practicum.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.validation.ValidUsername;

import java.util.List;
import java.util.Map;

public interface ShoppingCartApi {

    // Добавить товар в корзину.
    @PutMapping("/api/v1/shopping-cart")
    @ResponseStatus(HttpStatus.OK)
    ShoppingCartDto addProductsToCart(
            @RequestParam(required = true) @ValidUsername String username,
            @RequestBody @Valid @NotNull @NotEmpty Map<@NotNull String, @Positive Long> addProductMap
    );

    // Удалить указанные товары из корзины пользователя.
    @PostMapping("/api/v1/shopping-cart/remove")
    @ResponseStatus(HttpStatus.OK)
    ShoppingCartDto removeProductsFromCart(
            @RequestParam(required = true) @ValidUsername String username,
            @RequestBody @Valid @NotNull @NotEmpty List<@NotNull String> removeProductList
    );

    // Изменить количество товаров в корзине.
    @PostMapping("/api/v1/shopping-cart/change-quantity")
    @ResponseStatus(HttpStatus.OK)
    ShoppingCartDto changeProductQuantity(
            @RequestParam(required = true) @ValidUsername String username,
            @RequestBody @Valid ChangeProductQuantityRequest changeProductQuantityRequest
    );

    // Деактивация корзины товаров для пользователя.
    @DeleteMapping("/api/v1/shopping-cart")
    @ResponseStatus(HttpStatus.OK)
    String deactivateCart(
            @RequestParam(required = true) @ValidUsername String username
    );

    // Получить актуальную корзину для авторизованного пользователя.
    @GetMapping("/api/v1/shopping-cart")
    @ResponseStatus(HttpStatus.OK)
    ShoppingCartDto getCartByUsername(
            @RequestParam(required = true) @ValidUsername String username
    );

}
