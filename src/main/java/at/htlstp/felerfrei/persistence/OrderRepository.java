package at.htlstp.felerfrei.persistence;

import at.htlstp.felerfrei.domain.order.Order;
import at.htlstp.felerfrei.domain.order.OrderContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    @Query("SELECT o FROM order_product o where o.id = ?1")
    List<OrderContent> findOrderContentByOrderId(Integer orderId);

}
