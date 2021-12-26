package at.htlstp.felerfrei.domain.order;

import at.htlstp.felerfrei.domain.Product;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

import javax.persistence.*;
import java.util.Objects;

@Entity(name = "order_product")
@Getter
public class OrderContent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="order_product_id")
    private Integer id;

    private Integer amount;

    private String extrawurscht;

    @Column(name = "retail_price")
    private Double retailPrice;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

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
}
