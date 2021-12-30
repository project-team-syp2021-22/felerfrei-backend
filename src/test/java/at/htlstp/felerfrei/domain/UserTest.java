package at.htlstp.felerfrei.domain;

import at.htlstp.felerfrei.domain.user.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void equals() {
        var user1 = new User(1, "Florian", "Kainrath", "test@email.com", null, true, null, new Role(1, RoleAuthority.ROLE_USER));
        var user2 = new User(1, "", "Florian", "test@email.com", null, true, null, new Role(1, RoleAuthority.ROLE_USER));
        assertEquals(user1, user2);
    }

    @Test
    void not_equals() {
        var user1 = new User(1, "Florian", "Kainrath", "test@email.com", null, true, null, new Role(1, RoleAuthority.ROLE_USER));
        var user2 = new User(2, "", "Florian", "test@email.com", null, true, null, new Role(1, RoleAuthority.ROLE_USER));
        assertNotEquals(user1, user2);
    }

    @Test
    void correct_hash_code() {
        var user1 = new User(1, "Florian", "Kainrath", "test@email.com", null, true, null, new Role(1, RoleAuthority.ROLE_USER));
        var user2 = new User(2, "", "Florian", "test@email.com", null, true, null, new Role(1, RoleAuthority.ROLE_USER));
        assertNotEquals(user1.hashCode(), user2.hashCode());
    }

}