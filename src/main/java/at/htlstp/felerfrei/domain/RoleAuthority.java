package at.htlstp.felerfrei.domain;

import org.springframework.security.core.GrantedAuthority;

public enum RoleAuthority implements GrantedAuthority {

    ROLE_USER, ROLE_ADMIN;

    @Override
    public String getAuthority() {
        return this.name();
    }
}
