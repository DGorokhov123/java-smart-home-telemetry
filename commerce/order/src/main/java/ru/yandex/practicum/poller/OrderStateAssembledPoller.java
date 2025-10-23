package ru.yandex.practicum.poller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import ru.yandex.practicum.client.DeliveryClient;
import ru.yandex.practicum.dal.Order;
import ru.yandex.practicum.dal.OrderRepository;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.delivery.DeliveryState;
import ru.yandex.practicum.dto.order.OrderState;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderStateAssembledPoller extends AbstractPoller<Order> {

    private final TransactionTemplate transactionTemplate;
    private final OrderRepository orderRepository;
    private final DeliveryClient deliveryClient;
    private final OrderStateCalculatedPoller orderStateCalculatedPoller;

    @Override
    protected Optional<Order> getEntity() {
        return orderRepository.findFirstByStateOrderByTouchedAtAsc(OrderState.ASSEMBLED);
    }

    @Override
    protected void handleNormally(Order order) {
        DeliveryDto deliveryDto = new DeliveryDto();
        deliveryDto.setFromAddress(order.getFromAddress().toDto());
        deliveryDto.setToAddress(order.getToAddress().toDto());
        deliveryDto.setOrderId(order.getOrderId());
        deliveryDto.setDeliveryState(DeliveryState.CREATED);

        DeliveryDto returnedDeliveryDto = deliveryClient.createNewDelivery(deliveryDto);
        order.setDeliveryId(returnedDeliveryDto.getDeliveryId());

        BigDecimal deliveryCost = deliveryClient.calculateDeliveryCost(order.toDto());
        order.setDeliveryPrice(deliveryCost);

        transactionTemplate.execute(status -> {
            order.setState(OrderState.CALCULATED);
            order.setModifiedAt(Instant.now());
            order.setTouchedAt(Instant.now());
            orderRepository.save(order);
            return null;
        });

        orderStateCalculatedPoller.touch();
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
            order.setState(OrderState.CALCULATION_FAILED);
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
