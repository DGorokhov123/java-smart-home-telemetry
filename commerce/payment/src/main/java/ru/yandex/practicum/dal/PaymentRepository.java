package ru.yandex.practicum.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.dto.payment.PaymentState;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {

    Optional<Payment> findByOrderId(String orderId);

    boolean existsByOrderId(String orderId);

    // METHODS FOR POLLERS

    Optional<Payment> findFirstByPaymentStateOrderByTouchedAtAsc(PaymentState state);

}
