package at.htlstp.felerfrei.payload.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;


@AllArgsConstructor
@Data
@NoArgsConstructor
public class PayedRequest {
    @NonNull
    Boolean payed;
}
