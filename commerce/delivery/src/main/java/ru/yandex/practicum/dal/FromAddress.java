package ru.yandex.practicum.dal;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.dto.warehouse.AddressDto;

import java.math.BigDecimal;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Entity
@Table(name = "from_addresses")
public class FromAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "country", nullable = false, length = 100)
    private String country;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "street", nullable = false, length = 100)
    private String street;

    @Column(name = "house", nullable = false, length = 50)
    private String house;

    @Column(name = "flat", nullable = false, length = 50)
    private String flat;

    @Column(name = "price_multiplicator", nullable = false)
    private BigDecimal priceMultiplicator;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public static FromAddress newEntityFromDto(AddressDto dto) {
        FromAddress address = new FromAddress();
        address.setCountry(dto.country());
        address.setCity(dto.city());
        address.setStreet(dto.street());
        address.setHouse(dto.house());
        address.setFlat(dto.flat());
        address.setPriceMultiplicator(BigDecimal.ONE);
        return address;
    }

    public AddressDto toDto() {
        return AddressDto.builder()
                .country(country)
                .city(city)
                .street(street)
                .house(house)
                .flat(flat)
                .build();
    }

}
