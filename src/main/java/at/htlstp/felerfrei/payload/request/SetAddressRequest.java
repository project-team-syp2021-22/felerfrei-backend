package at.htlstp.felerfrei.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class SetAddressRequest {
    @NonNull
    private String street;
    @NonNull
    private String streetnumber;
    @NonNull
    private String city;
    @NonNull
    private String zip;
}
