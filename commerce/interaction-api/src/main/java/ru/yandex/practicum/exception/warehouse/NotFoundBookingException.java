package ru.yandex.practicum.exception.warehouse;

public class NotFoundBookingException extends RuntimeException {

    public NotFoundBookingException(String message) {
        super(message);
    }

}
