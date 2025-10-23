package ru.yandex.practicum.dal;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.delivery.DeliveryState;

import java.time.Instant;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Entity
@Table(name = "deliveries", indexes = {@Index(name = "idx_delivery_state_touched_at", columnList = "delivery_state, touched_at")})
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(name = "delivery_id", nullable = false, length = 50)
    private String deliveryId;

    @ManyToOne
    @JoinColumn(name = "from_address", nullable = false)
    private FromAddress fromAddress;

    @ManyToOne
    @JoinColumn(name = "to_address", nullable = false)
    private ToAddress toAddress;

    @Column(name = "order_id", nullable = false, unique = true, length = 50)
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_state", nullable = false, length = 25)
    private DeliveryState deliveryState;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "modified_at", nullable = false)
    private Instant modifiedAt;

    @Column(name = "touched_at", nullable = false)
    private Instant touchedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public DeliveryDto toDto() {
        DeliveryDto deliveryDto = new DeliveryDto();
        deliveryDto.setDeliveryId(deliveryId);
        deliveryDto.setOrderId(orderId);
        deliveryDto.setDeliveryState(deliveryState);
        deliveryDto.setFromAddress(fromAddress.toDto());
        deliveryDto.setToAddress(toAddress.toDto());
        return deliveryDto;
    }

}
