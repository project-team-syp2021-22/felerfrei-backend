package at.htlstp.felerfrei.payload.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ModifyProductRequest {
    @NonNull
    private String name;
    @NonNull
    private String description;
    private double price;
    @NonNull
    private String material;

    private boolean published;
}
