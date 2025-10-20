package ru.yandex.practicum.poller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import ru.yandex.practicum.client.OrderClient;
import ru.yandex.practicum.client.WarehouseClient;
import ru.yandex.practicum.dal.Delivery;
import ru.yandex.practicum.dal.DeliveryRepository;
import ru.yandex.practicum.dto.delivery.DeliveryState;
import ru.yandex.practicum.dto.warehouse.ShippedToDeliveryRequest;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DeliveryStartingOrderPoller extends AbstractPoller<Delivery> {

    private final TransactionTemplate transactionTemplate;
    private final DeliveryRepository deliveryRepository;

    private final OrderClient orderClient;

    @Override
    protected Optional<Delivery> getEntity() {
        return deliveryRepository.findFirstByDeliveryStateOrderByTouchedAtAsc(DeliveryState.STARTING_ORDER);
    }

    @Override
    protected void handleNormally(Delivery delivery) {
        orderClient.startedOrderDelivery(delivery.getOrderId());

        transactionTemplate.execute(status -> {
            delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
            delivery.setModifiedAt(Instant.now());
            delivery.setTouchedAt(Instant.now());
            deliveryRepository.save(delivery);
            return null;
        });

    }

    @Override
    protected void handleForRetry(Delivery delivery) {
        transactionTemplate.execute(status -> {
            delivery.setTouchedAt(Instant.now());
            deliveryRepository.save(delivery);
            return null;
        });
    }

    @Override
    protected void handleIfTimeout(Delivery delivery) {
        transactionTemplate.execute(status -> {
            delivery.setDeliveryState(DeliveryState.FAILED_ORDER_NOTIFY);
            delivery.setModifiedAt(Instant.now());
            delivery.setTouchedAt(Instant.now());
            deliveryRepository.save(delivery);
            return null;
        });
    }

    @Override
    protected boolean exceedTimeoutCondition(Delivery delivery) {
        return Duration.between(Instant.now(), delivery.getModifiedAt()).abs().compareTo(Duration.ofMinutes(30)) > 0;
    }

    @Override
    protected String id(Delivery delivery) {
        return delivery.getDeliveryId();
    }

    @Override
    protected String name() {
        return "Delivery";
    }

}
