package at.htlstp.felerfrei.payload.request;

import at.htlstp.felerfrei.payload.response.JwtResponse;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Delegate;

public class ChangeCredentialsRequest {

    @Delegate
    private final JwtResponse data;

    @Getter
    private final String password;

    public ChangeCredentialsRequest(@NonNull String token, @NonNull String email, @NonNull String firstname,
                                    @NonNull String lastname, @NonNull String password, @NonNull String telephone) {
        data = new JwtResponse(token, email, firstname, lastname, telephone);
        this.password = password;
    }
}
