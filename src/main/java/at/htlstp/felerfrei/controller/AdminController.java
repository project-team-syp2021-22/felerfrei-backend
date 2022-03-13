package at.htlstp.felerfrei.controller;

import at.htlstp.felerfrei.domain.Product;
import at.htlstp.felerfrei.payload.request.AddProductRequest;
import at.htlstp.felerfrei.payload.response.MessageResponse;
import at.htlstp.felerfrei.persistence.ImageRepository;
import at.htlstp.felerfrei.persistence.ProductRepository;
import at.htlstp.felerfrei.services.FileService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/admin")
public class AdminController {

    private final ProductRepository productRepository;
    private final FileService imageLocationService;

    public AdminController(ProductRepository productRepository, FileService imageLocationService) {
        this.productRepository = productRepository;
        this.imageLocationService = imageLocationService;
    }

    @GetMapping("/products")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Page<Product> getProducts(Pageable pageable) {
        return productRepository.findAllByOrderById(pageable);
    }

    @PostMapping("/addProduct")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public MessageResponse addProduct(@RequestBody AddProductRequest addProductRequest) {
        var product = new Product(null, addProductRequest.getName(),
                addProductRequest.getDescription(), false,
                addProductRequest.getPrice(), addProductRequest.getMaterial(), null);
        productRepository.save(product);
        return new MessageResponse("Product added");
    }

    // TODO: add parameter for product id
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void upload(@RequestParam(value = "image") List<MultipartFile> files) {
        String directory = "/img";
        for (MultipartFile file : files) {
            imageLocationService.save(file, directory);
        }
    }
}
