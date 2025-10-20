package ru.yandex.practicum.dal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliverySettingsRepository extends JpaRepository<DeliverySettings, Long> {

    List<DeliverySettings> findAllByOrderByIdDesc();

    Optional<DeliverySettings> findFirstByOrderByIdDesc();

}
