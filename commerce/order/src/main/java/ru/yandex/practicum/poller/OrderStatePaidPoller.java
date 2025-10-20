package ru.yandex.practicum.poller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import ru.yandex.practicum.client.DeliveryClient;
import ru.yandex.practicum.dal.Order;
import ru.yandex.practicum.dal.OrderRepository;
import ru.yandex.practicum.dto.order.OrderState;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderStatePaidPoller extends AbstractPoller<Order> {

    private final TransactionTemplate transactionTemplate;
    private final OrderRepository orderRepository;
    private final DeliveryClient deliveryClient;

    @Override
    protected Optional<Order> getEntity() {
        return orderRepository.findFirstByStateOrderByTouchedAtAsc(OrderState.PAID);
    }

    @Override
    protected void handleNormally(Order order) {
        deliveryClient.startDelivery(order.getOrderId());
        transactionTemplate.execute(status -> {
            order.setState(OrderState.SENT_TO_DELIVERY);
            order.setModifiedAt(Instant.now());
            order.setTouchedAt(Instant.now());
            orderRepository.save(order);
            return null;
        });
    }

    @Override
    protected void handleForRetry(Order order) {
        transactionTemplate.execute(status -> {
            order.setTouchedAt(Instant.now());
            orderRepository.save(order);
            return null;
        });
    }

    @Override
    protected void handleIfTimeout(Order order) {
        transactionTemplate.execute(status -> {
            order.setState(OrderState.CREATE_DELIVERY_FAILED);
            order.setModifiedAt(Instant.now());
            order.setTouchedAt(Instant.now());
            orderRepository.save(order);
            return null;
        });
    }

    @Override
    protected boolean exceedTimeoutCondition(Order order) {
        return Duration.between(Instant.now(), order.getModifiedAt()).abs().compareTo(Duration.ofMinutes(30)) > 0;
    }

    @Override
    protected String id(Order order) {
        return order.getOrderId();
    }

    @Override
    protected String name() {
        return "Order";
    }

}
