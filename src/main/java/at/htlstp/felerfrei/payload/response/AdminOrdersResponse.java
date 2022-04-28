package at.htlstp.felerfrei.payload.response;

import at.htlstp.felerfrei.domain.order.Order;
import at.htlstp.felerfrei.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class AdminOrdersResponse {

    private Order order;

    private double totalPrice;

    private User user;

}
