package at.htlstp.felerfrei.payload.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginRequest {
    @NonNull
    private String email;
    @NonNull
    private String password;
}
