package ru.yandex.practicum.poller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import ru.yandex.practicum.client.OrderClient;
import ru.yandex.practicum.dal.Payment;
import ru.yandex.practicum.dal.PaymentRepository;
import ru.yandex.practicum.dto.payment.PaymentState;

import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PaymentStateFailPoller extends AbstractPoller<Payment> {

    private final TransactionTemplate transactionTemplate;
    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;

    @Override
    protected Optional<Payment> getEntity() {
        return paymentRepository.findFirstByPaymentStateOrderByTouchedAtAsc(PaymentState.FAIL);
    }

    @Override
    protected void handleNormally(Payment payment) {
        orderClient.failedPaymentForOrderId(payment.getOrderId());
        transactionTemplate.execute(status -> {
            payment.setPaymentState(PaymentState.FAILED);
            payment.setModifiedAt(Instant.now());
            payment.setTouchedAt(Instant.now());
            paymentRepository.save(payment);
            return null;
        });
    }

    @Override
    protected void handleForRetry(Payment payment) {
        transactionTemplate.execute(status -> {
            payment.setTouchedAt(Instant.now());
            paymentRepository.save(payment);
            return null;
        });
    }

    @Override
    protected void handleIfTimeout(Payment payment) {
        // do nothing. we MUST send payment info anyway!
        // we can notify here real people for manual processing
    }

    @Override
    protected boolean exceedTimeoutCondition(Payment payment) {
        return false;   // never exceeded
    }

    @Override
    protected String id(Payment payment) {
        return payment.getPaymentId();
    }

    @Override
    protected String name() {
        return "Payment";
    }

}
