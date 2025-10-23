package ru.yandex.practicum.dal;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.order.OrderState;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Entity
@Table(name = "orders", indexes = {@Index(name = "idx_order_state_touched_at", columnList = "order_state, touched_at")})
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(name = "order_id", nullable = false, length = 50)
    private String orderId;

    @Column(name = "shopping_cart_id", nullable = false, unique = true, length = 50)
    private String shoppingCartId;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @ElementCollection
    @CollectionTable(name = "order_products", joinColumns = @JoinColumn(name = "order_id"))
    @MapKeyColumn(name = "product_id", nullable = false, length = 50)
    @Column(name = "quantity", nullable = false)
    private Map<String, Long> products = new HashMap<>();

    @ManyToOne
    @JoinColumn(name = "from_address", nullable = true)
    private FromAddress fromAddress;

    @ManyToOne
    @JoinColumn(name = "to_address", nullable = false)
    private ToAddress toAddress;

    @Column(name = "payment_id", nullable = true, length = 50)
    private String paymentId;

    @Column(name = "delivery_id", nullable = true, length = 50)
    private String deliveryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_state", nullable = false, length = 30)
    private OrderState state;

    @Column(name = "delivery_weight", nullable = true)
    private BigDecimal deliveryWeight;

    @Column(name = "delivery_volume", nullable = true)
    private BigDecimal deliveryVolume;

    @Column(name = "is_fragile", nullable = true)
    private Boolean fragile;

    @Column(name = "total_price", nullable = true)
    private BigDecimal totalPrice;

    @Column(name = "delivery_price", nullable = true)
    private BigDecimal deliveryPrice;

    @Column(name = "product_price", nullable = true)
    private BigDecimal productPrice;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "modified_at", nullable = false)
    private Instant modifiedAt;

    @Column(name = "touched_at", nullable = false)
    private Instant touchedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    // PUBLIC METHODS

    public void updateFromDto(OrderDto dto) {
        if (dto.getShoppingCartId() != null && !Objects.equals(dto.getShoppingCartId(), this.shoppingCartId)) {
            this.shoppingCartId = dto.getShoppingCartId();
        }
        if (dto.getProducts() != null && !Objects.equals(dto.getProducts(), this.products)) {
            this.products = dto.getProducts();
        }
        if (dto.getPaymentId() != null && !Objects.equals(dto.getPaymentId(), this.paymentId)) {
            this.paymentId = dto.getPaymentId();
        }
        if (dto.getDeliveryId() != null && !Objects.equals(dto.getDeliveryId(), this.deliveryId)) {
            this.deliveryId = dto.getDeliveryId();
        }
        if (dto.getState() != null && !Objects.equals(dto.getState(), this.state)) {
            this.state = dto.getState();
        }
        if (dto.getDeliveryWeight() != null && !Objects.equals(dto.getDeliveryWeight(), this.deliveryWeight)) {
            this.deliveryWeight = dto.getDeliveryWeight();
        }
        if (dto.getDeliveryVolume() != null && !Objects.equals(dto.getDeliveryVolume(), this.deliveryVolume)) {
            this.deliveryVolume = dto.getDeliveryVolume();
        }
        if (dto.getFragile() != null && !Objects.equals(dto.getFragile(), this.fragile)) {
            this.fragile = dto.getFragile();
        }
        if (dto.getTotalPrice() != null && !Objects.equals(dto.getTotalPrice(), this.totalPrice)) {
            this.totalPrice = dto.getTotalPrice();
        }
        if (dto.getDeliveryPrice() != null && !Objects.equals(dto.getDeliveryPrice(), this.deliveryPrice)) {
            this.deliveryPrice = dto.getDeliveryPrice();
        }
        if (dto.getProductPrice() != null && !Objects.equals(dto.getProductPrice(), this.productPrice)) {
            this.productPrice = dto.getProductPrice();
        }
    }

    public OrderDto toDto() {
        OrderDto dto = new OrderDto();
        dto.setOrderId(orderId);
        dto.setShoppingCartId(shoppingCartId);
        dto.setProducts(products);
        dto.setPaymentId(paymentId);
        dto.setDeliveryId(deliveryId);
        dto.setState(state);
        dto.setDeliveryWeight(deliveryWeight);
        dto.setDeliveryVolume(deliveryVolume);
        dto.setFragile(fragile);
        dto.setTotalPrice(totalPrice);
        dto.setDeliveryPrice(deliveryPrice);
        dto.setProductPrice(productPrice);
        return dto;
    }

}
