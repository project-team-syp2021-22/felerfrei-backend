package at.htlstp.felerfrei.persistence;

import at.htlstp.felerfrei.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> {
}
