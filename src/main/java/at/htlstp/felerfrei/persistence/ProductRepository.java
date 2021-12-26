package at.htlstp.felerfrei.persistence;

import at.htlstp.felerfrei.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
