package at.htlstp.felerfrei.payload.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequest {

    public SignupRequest(@NonNull String email, @NonNull String password, @NonNull String firstname,
                         @NonNull String lastname) {
        this.email = email;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
    }

    @NonNull
    private String email;
    @NonNull
    private String password;
    @NonNull
    private String firstname;
    @NonNull
    private String lastname;
    private String telephone;

}
