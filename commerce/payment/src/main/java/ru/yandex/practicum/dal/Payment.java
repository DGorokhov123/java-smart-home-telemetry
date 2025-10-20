package ru.yandex.practicum.dal;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.dto.payment.PaymentState;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Entity
@Table(name = "payments", indexes = {@Index(name = "idx_payment_state_touched_at", columnList = "payment_state, touched_at")})
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(name = "payment_id", nullable = false, length = 50)
    private String paymentId;

    @Column(name = "order_id", nullable = false, unique = true, length = 50)
    private String orderId;

    @Column(name = "total_payment", nullable = false)
    private BigDecimal totalPayment;

    @Column(name = "delivery_total", nullable = false)
    private BigDecimal deliveryTotal;

    @Column(name = "fee_total", nullable = false)
    private BigDecimal feeTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_state", nullable = false)
    private PaymentState paymentState;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "modified_at", nullable = false)
    private Instant modifiedAt;

    @Column(name = "touched_at", nullable = false)
    private Instant touchedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public PaymentDto toDto() {
        PaymentDto dto = new PaymentDto();
        dto.setPaymentId(paymentId);
        dto.setTotalPayment(totalPayment);
        dto.setDeliveryTotal(deliveryTotal);
        dto.setFeeTotal(feeTotal);
        return dto;
    }

}
