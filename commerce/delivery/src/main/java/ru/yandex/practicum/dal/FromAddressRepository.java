package ru.yandex.practicum.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.dto.warehouse.AddressDto;

public interface FromAddressRepository extends JpaRepository<FromAddress, Long> {

    FromAddress findByCountryAndCityAndStreetAndHouseAndFlat(
            String country,
            String city,
            String street,
            String house,
            String flat
    );

    default FromAddress findByAddressDto(AddressDto dto) {
        return findByCountryAndCityAndStreetAndHouseAndFlat(
                dto.country(),
                dto.city(),
                dto.street(),
                dto.house(),
                dto.flat()
        );
    }

}
