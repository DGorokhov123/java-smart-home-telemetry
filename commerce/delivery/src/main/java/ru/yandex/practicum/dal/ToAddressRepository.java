package ru.yandex.practicum.dal;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ToAddressRepository extends JpaRepository<ToAddress, Long> {

    ToAddress findByCountryAndCityAndStreetAndHouseAndFlat(
            String country,
            String city,
            String street,
            String house,
            String flat
    );

}
