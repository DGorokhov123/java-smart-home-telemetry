package ru.yandex.practicum.dal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initFromAddressData(FromAddressRepository repository) {
        return args -> {
            String[][] adrArray = {
                    {"Russia", "Moscow", "Stroiteley", "25", "12", "1"},
                    {"USA", "New-York", "Wall Street", "1", "1", "2"}
            };
            for (String[] adr : adrArray) {
                FromAddress fromAddress = repository.findByCountryAndCityAndStreetAndHouseAndFlat(
                        adr[0], adr[1], adr[2], adr[3], adr[4]
                );
                if (fromAddress == null) {
                    fromAddress = new FromAddress();
                    fromAddress.setCountry(adr[0]);
                    fromAddress.setCity(adr[1]);
                    fromAddress.setStreet(adr[2]);
                    fromAddress.setHouse(adr[3]);
                    fromAddress.setFlat(adr[4]);
                    fromAddress.setPriceMultiplicator(new BigDecimal(adr[5]));
                    repository.save(fromAddress);
                }
            }
        };
    }

    @Bean
    @Transactional
    CommandLineRunner initDeliverySettingsData(DeliverySettingsRepository repository) {
        return args -> {
            if (repository.findAll().isEmpty()) {
                DeliverySettings settings = new DeliverySettings();
                settings.setBaseCost(new BigDecimal("5.0"));
                settings.setFragilityMultiplicator(new BigDecimal("0.2"));
                settings.setWeightMultiplicator(new BigDecimal("0.3"));
                settings.setVolumeMultiplicator(new BigDecimal("0.2"));
                settings.setStreetMultiplicator(new BigDecimal("0.2"));
                repository.save(settings);
            }
        };
    }

}
