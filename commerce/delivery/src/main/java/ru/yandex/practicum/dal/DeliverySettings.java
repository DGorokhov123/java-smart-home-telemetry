package ru.yandex.practicum.dal;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Entity
@Table(name = "delivery_settings")
public class DeliverySettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "base_cost", nullable = false)
    private BigDecimal baseCost;

    @Column(name = "fragility_multiplicator", nullable = false)
    private BigDecimal fragilityMultiplicator;

    @Column(name = "weight_multiplicator", nullable = false)
    private BigDecimal weightMultiplicator;

    @Column(name = "volume_multiplicator", nullable = false)
    private BigDecimal volumeMultiplicator;

    @Column(name = "street_multiplicator", nullable = false)
    private BigDecimal streetMultiplicator;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

}
