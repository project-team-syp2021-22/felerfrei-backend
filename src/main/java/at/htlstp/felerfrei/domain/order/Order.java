package at.htlstp.felerfrei.domain.order;

import at.htlstp.felerfrei.domain.user.User;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "\"order\"")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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

    @OneToMany(mappedBy="order", fetch=FetchType.EAGER, cascade = CascadeType.ALL)
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

    public void addOrderContent(@NonNull OrderContent content) {
        if(this.orderContents== null) {
            this.orderContents = new ArrayList<>();
        }

        this.orderContents.add(content);
        content.setOrder(this);
    }

    public void setOrderContent(List<OrderContent> orderContents) {
        for(var c : orderContents) {
            addOrderContent(c);
        }
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", orderdate=" + orderdate +
                ", ordered=" + ordered +
                ", orderAddress='" + orderAddress + '\'' +
                ", user=" + user +
                ", orderContents=" + orderContents +
                '}';
    }
}