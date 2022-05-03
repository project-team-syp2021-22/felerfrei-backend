package at.htlstp.felerfrei.payload.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderRequest {

    private boolean delivery;

    @NonNull
    private String street = "";
    @NonNull
    private String houseNumber = "";
    @NonNull
    private String city = "";
    @NonNull
    private String zip = "";
}
