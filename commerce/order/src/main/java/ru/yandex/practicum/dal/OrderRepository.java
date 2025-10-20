package ru.yandex.practicum.dal;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.dto.order.OrderState;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {

    Optional<Order> findByShoppingCartId(String shoppingCartId);

    List<Order> findAllByUsernameOrderByCreatedAtDesc(String username);

    // METHODS FOR POLLERS

    @EntityGraph(attributePaths = {"products", "fromAddress", "toAddress"})
    Optional<Order> findFirstByStateOrderByTouchedAtAsc(OrderState state);

}
