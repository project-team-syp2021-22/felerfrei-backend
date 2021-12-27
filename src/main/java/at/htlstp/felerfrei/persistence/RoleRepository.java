package at.htlstp.felerfrei.persistence;

import at.htlstp.felerfrei.domain.Role;
import at.htlstp.felerfrei.domain.RoleAuthority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    @Query("select r from Role r where r.name = ?1")
    Optional<Role> findByName(RoleAuthority name);

}
