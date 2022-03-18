package at.htlstp.felerfrei.controller;

import at.htlstp.felerfrei.domain.Product;
import at.htlstp.felerfrei.payload.request.AddProductRequest;
import at.htlstp.felerfrei.persistence.ProductRepository;
import at.htlstp.felerfrei.services.FileService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
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
    public ResponseEntity<Integer> addProduct(@RequestBody AddProductRequest addProductRequest) {
        var product = new Product(null, addProductRequest.getName(),
                addProductRequest.getDescription(), false,
                addProductRequest.getPrice(), addProductRequest.getMaterial(), null);
        var saved = productRepository.save(product);
        return ResponseEntity.ok(saved.getId());
    }

    @PutMapping("/updateProduct/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Integer> updateProduct(@PathVariable("id") Integer id, @RequestBody AddProductRequest addProductRequest) {
        var product = productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        product.setName(addProductRequest.getName());
        product.setDescription(addProductRequest.getDescription());
        product.setPrice(addProductRequest.getPrice());
        product.setMaterial(addProductRequest.getMaterial());
        productRepository.save(product);
        return ResponseEntity.ok(product.getId());
    }

    @PostMapping("/uploadImage/{productId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void upload(@RequestParam(value = "images") List<MultipartFile> files, @PathVariable Integer productId) {
        String directory = "/img";
        var imageIds = new ArrayList<Integer>();
        for (MultipartFile file : files) {
            int imageId = imageLocationService.save(file, directory);
            imageIds.add(imageId);
        }

        var product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        for(var imageId : imageIds) {
            var image = imageLocationService.getImage(imageId).orElseThrow(() -> new IllegalArgumentException("Image not found"));
            product.addImage(image);
        }
        productRepository.save(product);
    }

    /**
     * Removes an image from a product. If an image is removed from a product, the image is not deleted from the file system.
     * However, if the image is not used by any other product, it will be deleted from the file system.
     * @param productId id of the product where the image should be deleted
     * @param imageId id of the image that should be deleted
     */
    @PostMapping("/removeImage/{productId}/{imageId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void removeImage(@PathVariable Integer productId, @PathVariable Integer imageId) {
        var product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        product.removeImage(imageId);
        productRepository.save(product);
        imageLocationService.deleteIfNotUsed(imageId);
    }
}
