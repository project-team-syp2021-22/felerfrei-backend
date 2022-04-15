package at.htlstp.felerfrei.services.pdf;

import at.htlstp.felerfrei.domain.order.Order;

public interface OrderConfirmationService {

    void write(Order order);

}
