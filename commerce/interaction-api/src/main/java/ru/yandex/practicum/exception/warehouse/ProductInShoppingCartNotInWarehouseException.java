package ru.yandex.practicum.exception.warehouse;

public class ProductInShoppingCartNotInWarehouseException extends RuntimeException {

    public ProductInShoppingCartNotInWarehouseException(String message) {
        super(message);
    }

}
