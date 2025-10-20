package ru.yandex.practicum.dal;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FromAddressRepository extends JpaRepository<FromAddress, Long> {

    FromAddress findByCountryAndCityAndStreetAndHouseAndFlat(
            String country,
            String city,
            String street,
            String house,
            String flat
    );

}
