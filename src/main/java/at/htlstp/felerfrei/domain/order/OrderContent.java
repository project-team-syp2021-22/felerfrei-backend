package at.htlstp.felerfrei.domain.order;

import at.htlstp.felerfrei.domain.Product;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Entity(name = "order_product")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderContent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="order_product_id")
    private Integer id;

    @Setter
    private Integer amount;

    private String extrawurscht;

    @Column(name = "retail_price")
    private Double retailPrice;

    @ManyToOne(targetEntity = Order.class)
    @JoinColumn(name = "order_id")
    @JsonIgnore
    @Setter
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    public void removeFromOrder() {
        this.order = null;
    }

    public static int MAX_AMOUNT = 10;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderContent that = (OrderContent) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "OrderContent{" +
                "id=" + id +
                ", amount=" + amount +
                ", extrawurscht='" + extrawurscht + '\'' +
                ", retailPrice=" + retailPrice +
                ", product=" + product +
                '}';
    }
}
