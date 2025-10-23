package ru.yandex.practicum.dto.delivery;

public enum DeliveryState {

    CREATED,
    STARTING,
    STARTING_WAREHOUSE,
    STARTING_ORDER,
    IN_PROGRESS,
    FAILED_WAREHOUSE,
    FAILED_ORDER_NOTIFY,
    DELIVERED,
    FAILED,
    CANCELLED,
    COMPLETED

}
