package at.htlstp.felerfrei.domain.user;

import at.htlstp.felerfrei.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.util.Objects;

@Entity
@Table(name = "\"user\"")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "firstname", nullable = false, length = 128)
    private String firstname;

    @Column(name = "lastname", nullable = false, length = 128)
    private String lastname;

    @Column(name = "email", nullable = false, length = 248, unique = true)
    @Email
    private String email;

    @Column(name = "password", length = 2048)
    private String password;

    @Column(name = "enabled", nullable = false)
    @Setter
    private Boolean enabled = false;

    @Column(name = "telephonenumber", length = 30)
    private String telephonenumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    public User(String firstname, String lastname, String email, String password, String telephonenumber) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
        this.telephonenumber = telephonenumber;
    }

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

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", enabled=" + enabled +
                ", telephonenumber='" + telephonenumber + '\'' +
                ", role=" + role +
                '}';
    }
}