package ru.yandex.practicum.dal;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.dto.delivery.DeliveryState;

import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, String> {

    Optional<Delivery> findByOrderId(String orderId);

    boolean existsByOrderId(String orderId);

    // METHODS FOR POLLERS

    @EntityGraph(attributePaths = {"fromAddress", "toAddress"})
    Optional<Delivery> findFirstByDeliveryStateOrderByTouchedAtAsc(DeliveryState state);

}
