package ru.yandex.practicum.client;

import org.springframework.cloud.openfeign.FallbackFactory;

public class ShoppingStoreClientFallbackFactory implements FallbackFactory<ShoppingStoreClient> {

    @Override
    public ShoppingStoreClient create(Throwable cause) {
        return null;
    }

}
