package at.htlstp.felerfrei.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AddProductRequest {
    private String name;
    private String description;
    private double price;
    private String material;
}
