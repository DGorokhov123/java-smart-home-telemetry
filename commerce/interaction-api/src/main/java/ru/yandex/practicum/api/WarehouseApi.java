package ru.yandex.practicum.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.*;

import java.util.Map;

public interface WarehouseApi {

    // Добавить новый товар на склад.
    @PutMapping("/api/v1/warehouse")
    @ResponseStatus(HttpStatus.OK)
    String createProduct(
            @RequestBody @Valid NewProductInWarehouseRequest newProductInWarehouseRequest
    );

    // Предварительно проверить что количество товаров на складе достаточно для данной корзины продуктов.
    @PostMapping("/api/v1/warehouse/check")
    @ResponseStatus(HttpStatus.OK)
    BookedProductsDto checkShoppingCart(
            @RequestBody @Valid ShoppingCartDto shoppingCartDto
    );

    // Принять товар на склад. (изменить кол-во товара)
    @PostMapping("/api/v1/warehouse/add")
    @ResponseStatus(HttpStatus.OK)
    String changeQuantity(
            @RequestBody @Valid AddProductToWarehouseRequest addProductToWarehouseRequest
    );

    // Предоставить адрес склада для расчёта доставки.
    @GetMapping("/api/v1/warehouse/address")
    @ResponseStatus(HttpStatus.OK)
    AddressDto getAddress();

    // Передать товары в доставку.
    @PostMapping("/api/v1/warehouse/shipped")
    @ResponseStatus(HttpStatus.OK)
    String sendToDelivery(
            @RequestBody @Valid ShippedToDeliveryRequest shippedToDeliveryRequest
    );

    // Принять возврат товаров на склад.
    @PostMapping("/api/v1/warehouse/return")
    @ResponseStatus(HttpStatus.OK)
    String returnProducts(
            @RequestBody @NotNull @NotEmpty Map<String, Long> products
    );

    // Собрать товары к заказу для подготовки к отправке.
    @PostMapping("/api/v1/warehouse/assembly")
    @ResponseStatus(HttpStatus.OK)
    BookedProductsDto assemblyProductsForOrder(
            @RequestBody @Valid AssemblyProductsForOrderRequest assemblyProductsForOrderRequest
    );

    // Откатить сборку товаров
    @PostMapping("/api/v1/warehouse/assembly/rollback")
    @ResponseStatus(HttpStatus.OK)
    String rollbackBooking(
            @RequestBody @NotBlank String orderId
    );

    // Списать забронированные товары после доставки
    @PostMapping("/api/v1/warehouse/writeoff")
    @ResponseStatus(HttpStatus.OK)
    String writeOffBookedProducts(
            @RequestBody @NotBlank String orderId
    );

}
