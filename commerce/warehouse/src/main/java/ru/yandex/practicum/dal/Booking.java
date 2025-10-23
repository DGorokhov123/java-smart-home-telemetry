package ru.yandex.practicum.dal;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.dto.warehouse.BookingState;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(name = "booking_id", nullable = false, length = 50)
    private String bookingId;

    @Column(name = "order_id", nullable = false, unique = true, length = 50)
    private String orderId;

    @Column(name = "delivery_id", nullable = true, length = 50)
    private String deliveryId;

    @Column(name = "delivery_weight", nullable = false)
    private BigDecimal deliveryWeight;

    @Column(name = "delivery_volume", nullable = false)
    private BigDecimal deliveryVolume;

    @Column(name = "fragile", nullable = false)
    private Boolean fragile;

    @ElementCollection
    @CollectionTable(name = "booked_products", joinColumns = @JoinColumn(name = "booking_id"))
    @MapKeyColumn(name = "product_id", nullable = false, length = 50)
    @Column(name = "quantity", nullable = false)
    private Map<String, Long> products = new HashMap<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_state", nullable = false, length = 15)
    private BookingState bookingState;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public BookedProductsDto toBookedProductsDto() {
        BookedProductsDto bookedProductsDto = new BookedProductsDto();
        bookedProductsDto.setDeliveryWeight(deliveryWeight);
        bookedProductsDto.setDeliveryVolume(deliveryVolume);
        bookedProductsDto.setFragile(fragile);
        return bookedProductsDto;
    }

}
