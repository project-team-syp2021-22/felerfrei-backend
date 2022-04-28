package at.htlstp.felerfrei.domain.order;

import at.htlstp.felerfrei.domain.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @Setter
    private LocalDate orderdate;

    @Column(name = "isordered", nullable = false)
    private Boolean ordered = false;

    @Column(name="street")
    private String street;

    @Column(name="streetnumber")
    private String streetnumber;

    @Column(name="zipcode")
    private String zipcode;

    @Column(name="city")
    private String city;

    @Column(name = "payed", nullable = false, columnDefinition = "boolean default false")
    private boolean payed = false;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "order", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<OrderContent> orderContents;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    public double calculateTotalPrice() {
        double totalPrice = 0;
        for (OrderContent orderContent : orderContents) {
            totalPrice += orderContent.getAmount() * orderContent.getRetailPrice();
        }
        return totalPrice;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public void addOrderContent(@NonNull OrderContent content) {
        if (this.orderContents == null) {
            this.orderContents = new ArrayList<>();
        }
        for (var orderContent : orderContents) {
            if (equalOrderContent(orderContent, content)) {
                if (orderContent.getAmount() == OrderContent.MAX_AMOUNT) {
                    return;
                }
                orderContent.setAmount(orderContent.getAmount() + 1);
                return;
            }
        }
        content.setOrder(this);
        this.orderContents.add(content);
    }

    private boolean equalOrderContent(@NonNull OrderContent content1, @NonNull OrderContent content2) {
        if (!content1.getProduct().equals(content2.getProduct()))
            return false;
        if (content1.getExtrawurscht() == null && content2.getExtrawurscht() == null)
            return true;
        if (content1.getExtrawurscht() == null || content2.getExtrawurscht() == null)
            return false;
        return content1.getExtrawurscht().equals(content2.getExtrawurscht());
    }

    public void setOrderContent(List<OrderContent> orderContents) {
        for (var c : orderContents) {
            addOrderContent(c);
        }
    }
}