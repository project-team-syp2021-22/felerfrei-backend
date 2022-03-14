package at.htlstp.felerfrei.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@Getter
public class AddProductRequest {
    @NonNull
    private String name;
    @NonNull
    private String description;
    private double price;
    @NonNull
    private String material;
}
