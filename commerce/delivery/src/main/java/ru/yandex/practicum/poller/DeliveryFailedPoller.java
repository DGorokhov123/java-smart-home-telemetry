package ru.yandex.practicum.poller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import ru.yandex.practicum.client.OrderClient;
import ru.yandex.practicum.dal.Delivery;
import ru.yandex.practicum.dal.DeliveryRepository;
import ru.yandex.practicum.dto.delivery.DeliveryState;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DeliveryFailedPoller extends AbstractPoller<Delivery> {

    private final TransactionTemplate transactionTemplate;
    private final DeliveryRepository deliveryRepository;

    private final OrderClient orderClient;

    @Override
    protected Optional<Delivery> getEntity() {
        return deliveryRepository.findFirstByDeliveryStateOrderByTouchedAtAsc(DeliveryState.FAILED);
    }

    @Override
    protected void handleNormally(Delivery delivery) {
        orderClient.failedOrderDelivery(delivery.getOrderId());
        transactionTemplate.execute(status -> {
            delivery.setDeliveryState(DeliveryState.CANCELLED);
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
