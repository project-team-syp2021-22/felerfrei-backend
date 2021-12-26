package at.htlstp.felerfrei.domain.order;

import at.htlstp.felerfrei.domain.User;
import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "\"order\"")
@Getter
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "orderdate", nullable = false)
    private LocalDate orderdate;

    @Column(name = "isordered", nullable = false)
    private Boolean ordered = false;

    @Column(name = "order_address", length = 1024)
    private String orderAddress;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy="order")
    private List<OrderContent> orderContents;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}