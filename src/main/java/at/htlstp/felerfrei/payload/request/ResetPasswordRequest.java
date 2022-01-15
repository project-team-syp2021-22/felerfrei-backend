package at.htlstp.felerfrei.payload.request;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRequest {
    @NonNull
    private String token;

    @NonNull
    private String newPassword;
}
