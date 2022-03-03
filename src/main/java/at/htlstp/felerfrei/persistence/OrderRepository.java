package at.htlstp.felerfrei.persistence;

import at.htlstp.felerfrei.domain.order.Order;
import at.htlstp.felerfrei.domain.order.OrderContent;
import at.htlstp.felerfrei.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    @Query("SELECT o FROM order_product o where o.id = ?1")
    List<OrderContent> findOrderContentByOrderId(Integer orderId);

    @Query("SELECT o FROM Order o where o.user = ?1")
    List<Order> findByUser(User user);

    @Query("SELECT o FROM Order o where o.user = ?1 and o.ordered = false")
    Optional<Order> findCartByUser(User user);
}
