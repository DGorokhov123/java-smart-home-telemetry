package ru.yandex.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.api.ShoppingStoreApi;

@FeignClient(name = "shopping-store")
public interface ShoppingStoreClient extends ShoppingStoreApi {

}
