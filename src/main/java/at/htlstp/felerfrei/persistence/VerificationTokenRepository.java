package at.htlstp.felerfrei.persistence;

import at.htlstp.felerfrei.domain.user.User;
import at.htlstp.felerfrei.domain.user.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    VerificationToken findByUser(User user);

}
