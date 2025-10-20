package ru.yandex.practicum.poller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import ru.yandex.practicum.client.PaymentClient;
import ru.yandex.practicum.dal.Order;
import ru.yandex.practicum.dal.OrderRepository;
import ru.yandex.practicum.dto.order.OrderState;
import ru.yandex.practicum.dto.payment.PaymentDto;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderStateCalculatedPoller extends AbstractPoller<Order> {

    private final TransactionTemplate transactionTemplate;
    private final OrderRepository orderRepository;
    private final PaymentClient paymentClient;

    @Override
    protected Optional<Order> getEntity() {
        return orderRepository.findFirstByStateOrderByTouchedAtAsc(OrderState.CALCULATED);
    }

    @Override
    protected void handleNormally(Order order) {
        BigDecimal productPrice = paymentClient.calculateProductCostForOrder(order.toDto());
        order.setProductPrice(productPrice);

        BigDecimal totalPrice = paymentClient.calculateTotalCostForOrder(order.toDto());
        order.setTotalPrice(totalPrice);

        PaymentDto paymentDto = paymentClient.makePaymentForOrder(order.toDto());
        order.setPaymentId(paymentDto.getPaymentId());

        transactionTemplate.execute(status -> {
            order.setState(OrderState.ON_PAYMENT);
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
            order.setState(OrderState.CREATE_PAYMENT_FAILED);
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
