package ru.yandex.practicum.dal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, String> {

    Optional<Booking> findByOrderId(String orderId);

    boolean existsByOrderId(String orderId);

    void deleteByOrderId(String orderId);

}
