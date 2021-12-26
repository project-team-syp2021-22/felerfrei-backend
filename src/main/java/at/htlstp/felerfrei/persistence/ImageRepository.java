package at.htlstp.felerfrei.persistence;

import at.htlstp.felerfrei.domain.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Integer> {
}
