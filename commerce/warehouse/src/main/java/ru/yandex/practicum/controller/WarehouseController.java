package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.WarehouseApi;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.*;
import ru.yandex.practicum.service.WarehouseService;

import java.util.Map;

@Validated
@RestController
@RequiredArgsConstructor
public class WarehouseController implements WarehouseApi {

    private final WarehouseService warehouseService;

    // Добавить новый товар на склад.
    @Override
    public String createProduct(NewProductInWarehouseRequest newProductInWarehouseRequest) {
        return warehouseService.createProduct(newProductInWarehouseRequest);
    }

    // Предварительно проверить что количество товаров на складе достаточно для данной корзины продуктов.
    @Override
    public BookedProductsDto checkShoppingCart(ShoppingCartDto shoppingCartDto) {
        return warehouseService.checkShoppingCart(shoppingCartDto);
    }

    // Принять товар на склад. (изменить кол-во товара)
    @Override
    public String changeQuantity(AddProductToWarehouseRequest addProductToWarehouseRequest) {
        return warehouseService.changeQuantity(addProductToWarehouseRequest);
    }

    // Предоставить адрес склада для расчёта доставки.
    @Override
    public AddressDto getAddress() {
        return warehouseService.getAddress();
    }

    // Передать товары в доставку.
    @Override
    public String sendToDelivery(ShippedToDeliveryRequest shippedToDeliveryRequest) {
        return warehouseService.sendToDelivery(shippedToDeliveryRequest);
    }

    // Принять возврат товаров на склад.
    @Override
    public String returnProducts(Map<String, Long> products) {
        return warehouseService.returnProducts(products);
    }

    // Собрать товары к заказу для подготовки к отправке.
    @Override
    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest assemblyProductsForOrderRequest) {
        return warehouseService.assemblyProductsForOrder(assemblyProductsForOrderRequest);
    }

    // Откатить сборку товаров
    @Override
    public String rollbackBooking(String orderId) {
        orderId = orderId.replaceAll("\"", "");       // remove quotes
        return warehouseService.rollbackBooking(orderId);
    }

    @Override
    public String writeOffBookedProducts(String orderId) {
        orderId = orderId.replaceAll("\"", "");       // remove quotes
        return warehouseService.writeOffBookedProducts(orderId);
    }

}
