package ru.yandex.practicum.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.dto.warehouse.AddressDto;

public interface ToAddressRepository extends JpaRepository<ToAddress, Long> {

    ToAddress findByCountryAndCityAndStreetAndHouseAndFlat(
            String country,
            String city,
            String street,
            String house,
            String flat
    );

    default ToAddress findByAddressDto(AddressDto dto) {
        return findByCountryAndCityAndStreetAndHouseAndFlat(
                dto.getCountry(),
                dto.getCity(),
                dto.getStreet(),
                dto.getHouse(),
                dto.getFlat()
        );
    }

}
