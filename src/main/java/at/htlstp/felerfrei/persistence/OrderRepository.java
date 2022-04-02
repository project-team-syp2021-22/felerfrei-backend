package at.htlstp.felerfrei.persistence;

import at.htlstp.felerfrei.domain.order.Order;
import at.htlstp.felerfrei.domain.order.OrderContent;
import at.htlstp.felerfrei.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    @Query("SELECT o FROM order_product o where o.id = ?1")
    List<OrderContent> findOrderContentByOrderId(Integer orderId);

    @Query("SELECT o FROM Order o where o.user = ?1")
    List<Order> findByUser(User user);

    @Query("SELECT o FROM Order o where o.user = ?1 and o.ordered = false")
    Optional<Order> findCartByUser(User user);

    @Modifying
    @Transactional
    @Query("delete from order_product o where o.amount <= 0")
    void deleteEmptyContent();

    @Query("select o from order_product o where o.id = ?1")
    Optional<OrderContent> findOrderContentById(Integer id);

    @Modifying
    @Transactional
    @Query("delete from order_product o where o.id = ?1")
    void deleteOrderContentById(Integer id);

    @Query("""
            select o
            from Order o
            where (select content from order_product content where content.product.id = ?1 and content.order = o) member of o.orderContents
            and o.ordered = false
            """)
    @Transactional
    List<Order> findAllByNotOrderedOrderContainingProduct(Integer productId);

    @Query(value = "select remove_product_from_cart(?1, ?2)", nativeQuery = true)
    @Transactional
    boolean removeProductFromCart(Integer order_content_id, Integer delAmount);

    @Query(value="select set_order_content_amount(?1, ?2);", nativeQuery = true)
    boolean setOrderContentAmount(int orderContentId, int amount);
}
