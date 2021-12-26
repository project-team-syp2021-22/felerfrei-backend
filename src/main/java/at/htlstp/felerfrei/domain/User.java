package at.htlstp.felerfrei.domain;

import lombok.Getter;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "\"User\"")
@Getter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "firstname", nullable = false, length = 128)
    private String firstname;

    @Column(name = "lastname", nullable = false, length = 128)
    private String lastname;

    @Column(name = "email", nullable = false, length = 248)
    private String email;

    @Column(name = "password", length = 2048)
    private String password;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = false;

    @Column(name = "telephonenumber", length = 30)
    private String telephonenumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}