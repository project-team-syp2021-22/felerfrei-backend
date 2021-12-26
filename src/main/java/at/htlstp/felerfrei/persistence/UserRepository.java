package at.htlstp.felerfrei.persistence;

import at.htlstp.felerfrei.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
}
