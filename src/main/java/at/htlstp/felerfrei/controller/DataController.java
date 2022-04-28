package at.htlstp.felerfrei.controller;

import at.htlstp.felerfrei.domain.Product;
import at.htlstp.felerfrei.domain.Project;
import at.htlstp.felerfrei.domain.RoleAuthority;
import at.htlstp.felerfrei.persistence.OrderRepository;
import at.htlstp.felerfrei.persistence.ProductRepository;
import at.htlstp.felerfrei.persistence.ProjectRepository;
import at.htlstp.felerfrei.persistence.UserRepository;
import at.htlstp.felerfrei.security.jwt.JwtUtils;
import at.htlstp.felerfrei.services.FileService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class DataController {

    private final ProductRepository productRepository;
    private final ProjectRepository projectRepository;
    private final FileService imageLocationService;

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    private final JwtUtils jwtUtils;


    public DataController(ProductRepository productRepository, ProjectRepository projectRepository,
                          FileService imageLocationService, OrderRepository orderRepository, UserRepository userRepository, JwtUtils jwtUtils) {
        this.productRepository = productRepository;
        this.projectRepository = projectRepository;
        this.imageLocationService = imageLocationService;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
    }


    /**
     * Returns a list of all products which are already published.
     * <pre>
     * How to use:
     * <code>
     * get(".../api/products?size=10&page=0")
     * </code>Returns a page of products with size 10
     * </pre>
     * If you want to sort the products you can use the following query parameters:
     * <pre>
     *      ?sort=name,asc
     *      ?sort=name,desc
     *      ?sort=price,asc
     *      ?sort=price,desc
     *      ?sort=id,asc
     *      ?sort=id,desc
     *      ...
     *  </pre>
     * For more filtering or sorting options follow the documentation of spring data jpa.
     *
     * @param pageable defines the page and the size of the result
     * @return A Page of products, depending on the pageable
     */
    @GetMapping("/products")
    public Page<Product> getProducts(Pageable pageable) {
        return productRepository.findAllByPublished(true, pageable);
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Integer id) {
        var found = productRepository.findById(id);
        if (found.isEmpty() || !found.get().isPublished()) {
            throw new IllegalArgumentException("not found");
        }
        return found.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/projects")
    public Page<Project> getProjects(Pageable pageable) {
        return projectRepository.findAllByPublished(true, pageable);
    }

    @GetMapping(value = "/image/{id}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<Resource> image(@PathVariable int id) {
        var image = imageLocationService.get(id);

        if (image.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentLength(image.get().contentLength())
                .body(image.get());
    }


    @GetMapping(value = "/orderPdf/{orderId}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<InputStreamResource> getOrderPdf(@PathVariable Integer orderId, @RequestParam(value = "token") String token) {
        var email = jwtUtils.getEmailFromToken(token);

        var user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if(user.getRole().getName() != RoleAuthority.ROLE_ADMIN) {
            throw new IllegalArgumentException("User is not an admin");
        }

        var order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if(!order.getOrdered()) {
            throw new IllegalArgumentException("Order not ordered");
        }

        byte[] pdf;
        try {
            pdf = Files.readAllBytes(Path.of("orderconfirmations/" + orderId + ".pdf"));
        } catch (IOException e) {
            throw new IllegalArgumentException("No pdf found");
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(new ByteArrayInputStream(pdf)));
    }
}