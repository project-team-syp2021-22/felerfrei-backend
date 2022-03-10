package at.htlstp.felerfrei.controller;

import at.htlstp.felerfrei.domain.Product;
import at.htlstp.felerfrei.domain.Project;
import at.htlstp.felerfrei.domain.order.Order;
import at.htlstp.felerfrei.domain.order.OrderContent;
import at.htlstp.felerfrei.payload.request.AddTooCartRequest;
import at.htlstp.felerfrei.payload.request.RemoveFromCartRequest;
import at.htlstp.felerfrei.payload.request.SetProductInCartRequest;
import at.htlstp.felerfrei.payload.response.CartResponse;
import at.htlstp.felerfrei.payload.response.MessageResponse;
import at.htlstp.felerfrei.persistence.OrderRepository;
import at.htlstp.felerfrei.persistence.ProductRepository;
import at.htlstp.felerfrei.persistence.ProjectRepository;
import at.htlstp.felerfrei.persistence.UserRepository;
import at.htlstp.felerfrei.security.services.UserDetailsImpl;
import at.htlstp.felerfrei.services.FileService;
import at.htlstp.felerfrei.services.pdf.PDFOrderConfirmationService;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class DataController {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final OrderRepository orderRepository;
    private final FileService imageLocationService;
    private final PDFOrderConfirmationService orderConfirmationService;


    public DataController(ProductRepository productRepository, UserRepository userRepository, ProjectRepository projectRepository,
                          OrderRepository orderRepository, FileService imageLocationService,
                          PDFOrderConfirmationService orderConfirmationService) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.orderRepository = orderRepository;
        this.imageLocationService = imageLocationService;
        this.orderConfirmationService = orderConfirmationService;
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
        if (found.isEmpty() || !found.get().getPublished()) {
            throw new IllegalArgumentException("not found");
        }
        return found.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/projects")
    public Page<Project> getProjects(Pageable pageable) {
        return projectRepository.findAllByPublished(true, pageable);
    }

    @GetMapping("/orders")
    @PreAuthorize("hasRole('ROLE_USER')")
    public List<Order> getOrders() {
        return orderRepository.findAll();
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

    // TODO: remove this before deployment
    @PostMapping("/upload")
    public void upload(@RequestParam(value = "image") List<MultipartFile> files) {
        String directory = "/img";
        for (MultipartFile file : files) {
            imageLocationService.save(file, directory);
        }
    }

    @PutMapping("/addTooCart")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<MessageResponse> addTooCart(@RequestBody AddTooCartRequest request) {
        var user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var inDatabase = userRepository.findById(user.getId()).orElseThrow(() -> new IllegalArgumentException("no user"));

        var product = productRepository.findById(request.getProductId()).orElseThrow(() -> new IllegalArgumentException("product not found"));
        if (!product.getPublished()) {
            throw new IllegalArgumentException("product not found");
        }

        var cart = orderRepository.findCartByUser(inDatabase);
        if (cart.isEmpty()) {
            var order = new Order(null, LocalDate.now(), false, null, inDatabase, null);
            order.setOrderContent(List.of(new OrderContent(null, 1, "test", product.getPrice(), order, product)));
            orderRepository.save(order);
        } else {
            var optionalCart = cart.get();
            cart.get().addOrderContent(new OrderContent(null, request.getAmount(), request.getExtra(), product.getPrice(), cart.get(), product));
            orderRepository.save(cart.get());
        }

        return ResponseEntity.ok(new MessageResponse("okay"));
    }

    @GetMapping("/cart")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<CartResponse> cart() {
        var user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var inDatabase = userRepository.findById(user.getId()).orElseThrow(() -> new IllegalArgumentException("no user"));

        var cart = orderRepository.findCartByUser(inDatabase);
        if (cart.isEmpty()) {
            return ResponseEntity.ok(CartResponse.empty());
        } else {
            var optionalCart = cart.get();
            return ResponseEntity.ok(new CartResponse(optionalCart, optionalCart.calculateTotalPrice(),
                    optionalCart.getOrderContents().size(), optionalCart.getOrderContents().isEmpty()));
        }
    }

    @PutMapping("/setProductInCart")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<MessageResponse> setProductInCart(@RequestBody SetProductInCartRequest request) {
        if(request.getAmount() < 1) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }
        var user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var inDatabase = userRepository.findById(user.getId()).orElseThrow(() -> new IllegalArgumentException("no user"));

        var cart = orderRepository.findCartByUser(inDatabase);
        if (cart.isEmpty()) {
             return ResponseEntity.ok(new MessageResponse("no cart"));
        }
        var optionalCart = cart.get();

        var orderContent = orderRepository.findOrderContentById(request.getOrderContentId());
        if(orderContent.isEmpty()) {
            throw new IllegalArgumentException("order content not found");
        }
        if(orderContent.get().getOrder().getUser().getId() != inDatabase.getId()) {
            throw new IllegalArgumentException("you can't change other users order");
        }

        orderContent.get().setAmount(request.getAmount());
        orderRepository.save(optionalCart);
        return ResponseEntity.ok(new MessageResponse(String.valueOf(request.getAmount())));
    }

    @PutMapping("/deleteFromCart")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Void> removeFromCart(@RequestBody RemoveFromCartRequest request) {
        if(request.getAmount() <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }
        var user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var inDatabase = userRepository.findById(user.getId()).orElseThrow(() -> new IllegalArgumentException("no user"));

        var cart = orderRepository.findCartByUser(inDatabase);
        if (cart.isPresent()) {
            var optionalCart = cart.get();
            optionalCart.removeOrderContent(request.getOrderContentId(), request.getAmount());
            orderRepository.save(optionalCart);
            orderRepository.deleteOrderContentById(request.getOrderContentId());
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/clearCart")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<MessageResponse> clearCart() {
        var user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var inDatabase = userRepository.findById(user.getId()).orElseThrow(() -> new IllegalArgumentException("no user"));

        var cart = orderRepository.findCartByUser(inDatabase);
        if (cart.isEmpty()) {
            return ResponseEntity.ok(new MessageResponse("okay"));
        }
        var optionalCart = cart.get();
        orderRepository.delete(optionalCart);
        return ResponseEntity.ok(new MessageResponse("okay"));
    }
}
