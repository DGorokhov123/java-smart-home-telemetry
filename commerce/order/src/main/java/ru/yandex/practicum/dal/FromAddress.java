package ru.yandex.practicum.dal;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.dto.warehouse.AddressDto;

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

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public AddressDto toDto() {
        AddressDto addressDto = new AddressDto();
        addressDto.setCountry(country);
        addressDto.setCity(city);
        addressDto.setStreet(street);
        addressDto.setHouse(house);
        addressDto.setFlat(flat);
        return addressDto;
    }

}
