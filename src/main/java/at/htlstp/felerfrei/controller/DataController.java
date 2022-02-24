package at.htlstp.felerfrei.controller;

import at.htlstp.felerfrei.domain.Product;
import at.htlstp.felerfrei.domain.order.Order;
import at.htlstp.felerfrei.persistence.OrderRepository;
import at.htlstp.felerfrei.persistence.ProductRepository;
import at.htlstp.felerfrei.services.FileService;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class DataController {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final FileService imageLocationService;

    public DataController(ProductRepository productRepository, OrderRepository orderRepository, FileService imageLocationService) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.imageLocationService = imageLocationService;
    }


    /**
     * Returns a list of all products which are already published.
     * <pre>
     * How to use:
     * <code>
     * get(".../api/products?size=10&page=0")
     * </code>Returns a page of products with size 10
     * </pre>
     *  If you want to sort the products you can use the following query parameters:
     *  <pre>
     *      ?sort=name,asc
     *      ?sort=name,desc
     *      ?sort=price,asc
     *      ?sort=price,desc
     *      ?sort=id,asc
     *      ?sort=id,desc
     *      ...
     *  </pre>
     *  For more filtering or sorting options follow the documentation of spring data jpa.
     * @param pageable defines the page and the size of the result
     * @return A Page of products, depending on the pageable
     */
    @GetMapping("/products")
    public Page<Product> getProducts(Pageable pageable) {
        return productRepository.findAllByPublished(true, pageable);
    }

    @GetMapping("/orders")
    @PreAuthorize("hasRole('ROLE_USER')")
    public List<Order> getOrders() {
        return orderRepository.findAll();
    }


    @GetMapping(value = "/image/{id}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<Resource> image(@PathVariable int id) {
        var image = imageLocationService.get(id);

        if(image.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentLength(image.get().contentLength())
                .body(image.get());
    }

    @PostMapping("/upload")
    public void upload(@RequestParam(value= "image")List<MultipartFile> files) {
        String directory = "/img";
        for (MultipartFile file : files) {
            imageLocationService.save(file, directory);
        }
    }
}
