package at.htlstp.felerfrei.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {

    private String token;
    private String email;
    private String firstname;
    private String lastname;
    private String telephone;

    private String errorMessage = "";

    public JwtResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public JwtResponse(String token, String email, String firstname, String lastname, String telephone) {
        this.token = token;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.telephone = telephone;
    }
}
