package ru.yandex.practicum.poller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import ru.yandex.practicum.client.WarehouseClient;
import ru.yandex.practicum.dal.FromAddress;
import ru.yandex.practicum.dal.FromAddressRepository;
import ru.yandex.practicum.dal.Order;
import ru.yandex.practicum.dal.OrderRepository;
import ru.yandex.practicum.dto.order.OrderState;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderStateNewPoller extends AbstractPoller<Order> {

    private final TransactionTemplate transactionTemplate;
    private final OrderRepository orderRepository;
    private final FromAddressRepository fromAddressRepository;
    private final WarehouseClient warehouseClient;
    private final OrderStateAssembledPoller orderStateAssembledPoller;

    @Override
    protected Optional<Order> getEntity() {
        return orderRepository.findFirstByStateOrderByTouchedAtAsc(OrderState.NEW);
    }

    @Override
    protected void handleNormally(Order order) {
        AddressDto addressDto = warehouseClient.getAddress();

        AssemblyProductsForOrderRequest assemblyRequest = new AssemblyProductsForOrderRequest();
        assemblyRequest.setOrderId(order.getOrderId());
        assemblyRequest.setProducts(order.getProducts());

        BookedProductsDto bookedProductsDto = warehouseClient.assemblyProductsForOrder(assemblyRequest);
        order.setDeliveryWeight(bookedProductsDto.getDeliveryWeight());
        order.setDeliveryVolume(bookedProductsDto.getDeliveryVolume());
        order.setFragile(bookedProductsDto.getFragile());

        transactionTemplate.execute(status -> {
            FromAddress fromAddress = fromAddressRepository.findByAddressDto(addressDto);
            if (fromAddress == null) {
                fromAddress = FromAddress.newEntityFromDto(addressDto);
                fromAddressRepository.save(fromAddress);
            }
            order.setFromAddress(fromAddress);
            order.setState(OrderState.ASSEMBLED);
            order.setModifiedAt(Instant.now());
            order.setTouchedAt(Instant.now());
            orderRepository.save(order);
            return null;
        });

        orderStateAssembledPoller.touch();
    }

    @Override
    protected void handleForRetry(Order order) {
        order.setTouchedAt(Instant.now());
        transactionTemplate.execute(status -> {
            orderRepository.save(order);
            return null;
        });
    }

    @Override
    protected void handleIfTimeout(Order order) {
        order.setState(OrderState.ASSEMBLY_FAILED);
        order.setModifiedAt(Instant.now());
        order.setTouchedAt(Instant.now());
        transactionTemplate.execute(status -> {
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
