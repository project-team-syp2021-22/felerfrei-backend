package at.htlstp.felerfrei.domain.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "verification_token")
@Getter
@NoArgsConstructor
public class VerificationToken {
    private static final int EXPIRATION_TIME = 60 * 24;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Setter
    private LocalDateTime expiryDate;

    public VerificationToken(String token, User user) {
        this.token = token;
        this.user = user;
        this.expiryDate = calculateExpiryDate(EXPIRATION_TIME);
    }

    private LocalDateTime calculateExpiryDate(int expiryTimeInMinutes) {
        var date = LocalDateTime.now();
        return date.plusMinutes(expiryTimeInMinutes);
    }

    @Override
    public String toString() {
        return token;
    }
}
