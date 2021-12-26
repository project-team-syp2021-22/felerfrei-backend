package at.htlstp.felerfrei.persistence;

import at.htlstp.felerfrei.domain.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Integer> {
}
