package at.htlstp.felerfrei.domain;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "product")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class Product implements Showable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq")
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", length = 1024)
    private String description;

    @Column(name = "ispublished", nullable = false)
    private boolean published;

    @Column(name = "price")
    private Double price;

    @Column(name="material")
    private String material;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name="product_image",
            joinColumns=@JoinColumn(name="product_id"),
            inverseJoinColumns=@JoinColumn(name="image_id")
    )
    private List<Image> images;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public void addAllImages(@NonNull List<Image> images) {
        for(var i : images)
            addImage(i);
    }

    @Override
    public void removeImage(int id) {
        images.removeIf(image -> image.getId() == id);
    }

    @Override
    public void addImage(@NonNull Image image) {
        if(this.images == null) {
            this.images = new ArrayList<>();
        }
        this.images.add(image);
    }
}