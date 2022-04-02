package at.htlstp.felerfrei.persistence;

import at.htlstp.felerfrei.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    Page<Product> findAllByPublished(boolean published, Pageable pageable);

    Page<Product> findAllByOrderById(Pageable pageable);

    default void update(@Param("product") Product product) {
        update(product.getId(), product.getName(), product.getDescription(), product.getPrice(), product.getPublished(), product.getMaterial());
    }

    @Query(value = "select update_product(:productId, :name, :description, :price, :published, :material);",
            nativeQuery = true)
    @Transactional
    boolean update(Integer productId, String name, String description, double price, boolean published, String material);
}
