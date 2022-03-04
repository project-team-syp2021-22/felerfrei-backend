package at.htlstp.felerfrei.payload.response;

import at.htlstp.felerfrei.domain.order.Order;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CartResponse {

    private Order order;
    private double totalPrice;
    private int totalItems;


    public static CartResponse empty() {
        return new CartResponse(null, 0, 0);
    }
}
