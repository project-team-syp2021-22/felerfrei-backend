package at.htlstp.felerfrei.controller;

import at.htlstp.felerfrei.domain.Product;
import at.htlstp.felerfrei.domain.order.Order;
import at.htlstp.felerfrei.persistence.OrderRepository;
import at.htlstp.felerfrei.persistence.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class DataController {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public DataController(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }


    /**
     * Returns a list of all products which are already published.
     * <pre>
     * How to use:
     * <code>
     * get(".../api/products?size=10&page=0")
     * </code>Returns a page of products with size 10
     * </pre>
     *
     * @param pageable defines the page and the size of the result
     * @return A Page of products, depending on the pageable
     */
    @GetMapping("/products")
    public Page<Product> getProducts(Pageable pageable) {
        System.out.println(pageable.getPageSize());
        return productRepository.findAllByPublished(true, pageable);
    }

    @GetMapping("/orders")
    @PreAuthorize("hasRole('ROLE_USER')")
    public List<Order> getOrders() {
        return orderRepository.findAll();
    }

}
