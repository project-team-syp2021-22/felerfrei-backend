package at.htlstp.felerfrei.controller;

import at.htlstp.felerfrei.domain.RoleAuthority;
import at.htlstp.felerfrei.domain.order.Order;
import at.htlstp.felerfrei.domain.order.OrderContent;
import at.htlstp.felerfrei.payload.request.AddTooCartRequest;
import at.htlstp.felerfrei.payload.request.RemoveFromCartRequest;
import at.htlstp.felerfrei.payload.request.SetAddressRequest;
import at.htlstp.felerfrei.payload.request.SetProductInCartRequest;
import at.htlstp.felerfrei.payload.response.CartResponse;
import at.htlstp.felerfrei.payload.response.MessageResponse;
import at.htlstp.felerfrei.persistence.OrderRepository;
import at.htlstp.felerfrei.persistence.ProductRepository;
import at.htlstp.felerfrei.persistence.UserRepository;
import at.htlstp.felerfrei.security.services.UserDetailsImpl;
import at.htlstp.felerfrei.services.pdf.OrderConfirmationService;
import at.htlstp.felerfrei.services.pdf.PDFOrderConfirmationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class CartController {

    private final UserRepository userRepository;

    private final OrderRepository orderRepository;

    private final ProductRepository productRepository;

    private final OrderConfirmationService pdfConfirmationService;

    private final MailSender mailSender;

    public CartController(UserRepository userRepository, OrderRepository orderRepository, ProductRepository productRepository, OrderConfirmationService pdfConfirmationService, MailSender mailSender) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.pdfConfirmationService = pdfConfirmationService;
        this.mailSender = mailSender;
    }

    @PutMapping("/addTooCart")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<MessageResponse> addTooCart(@RequestBody AddTooCartRequest request) {
        var user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var inDatabase = userRepository.findById(user.getId()).orElseThrow(() -> new IllegalArgumentException("no user"));

        var product = productRepository.findById(request.getProductId()).orElseThrow(() -> new IllegalArgumentException("product not found"));
        if (!product.isPublished()) {
            throw new IllegalArgumentException("product not found");
        }

        var cart = orderRepository.findCartByUser(inDatabase);
        if (cart.isEmpty()) {
            var order = new Order(null, LocalDate.now(), false, null, null, null, null, false, inDatabase, null);
            order.setOrderContent(List.of(new OrderContent(null, 1, request.getExtra(), product.getPrice(), order, product)));
            orderRepository.save(order);
        } else {
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
        if (request.getAmount() < 1 || request.getAmount() > OrderContent.MAX_AMOUNT) {
            throw new IllegalArgumentException("amount must be a valid number");
        }
        var user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var inDatabase = userRepository.findById(user.getId()).orElseThrow(() -> new IllegalArgumentException("no user"));

        var cart = orderRepository.findCartByUser(inDatabase);
        if (cart.isEmpty()) {
            return ResponseEntity.ok(new MessageResponse("no cart"));
        }
        var orderContent = orderRepository.findOrderContentById(request.getOrderContentId());
        if (orderContent.isEmpty()) {
            throw new IllegalArgumentException("order content not found");
        }
        if (!Objects.equals(orderContent.get().getOrder().getUser().getId(), inDatabase.getId())) {
            throw new IllegalArgumentException("you can't change other users order");
        }

        orderRepository.setOrderContentAmount(request.getOrderContentId(), request.getAmount());
        return ResponseEntity.ok(new MessageResponse(String.valueOf(request.getAmount())));
    }

    @PutMapping("/deleteFromCart")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Void> removeFromCart(@RequestBody RemoveFromCartRequest request) {
        if (request.getAmount() <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }
        var user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var inDatabase = userRepository.findById(user.getId()).orElseThrow(() -> new IllegalArgumentException("no user"));

        var cart = orderRepository.findCartByUser(inDatabase);
        if (cart.isPresent()) {
            orderRepository.removeProductFromCart(request.getOrderContentId(), request.getAmount());
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

    @PostMapping("/order")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<MessageResponse> orderCart() {
        var user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var inDatabase = userRepository.findById(user.getId()).orElseThrow(() -> new IllegalArgumentException("no user"));
        var cart = orderRepository.findCartByUser(inDatabase).orElseThrow(() -> new IllegalArgumentException("no cart"));
        if (cart.getOrderContents().isEmpty()) {
            throw new IllegalArgumentException("cart is empty");
        }
        cart.setOrderdate(LocalDate.now());
        orderRepository.save(cart);

        pdfConfirmationService.write(cart);

        var path = String.format("%s/%s.pdf", PDFOrderConfirmationService.PATH, cart.getId());

        mailSender.sendOrderConfirmation(inDatabase.getEmail(), cart.getId(), path);

        orderRepository.orderCart(cart.getId());
        return ResponseEntity.ok(new MessageResponse("okay"));
    }

    /**
     * Method sets address for order. Call this before ordering the cart!
     *
     * @return
     */
    @PostMapping("/setAddress")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<MessageResponse> setAddressForOrder(@RequestBody SetAddressRequest request) {
        var user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var inDatabase = userRepository.findById(user.getId()).orElseThrow(() -> new IllegalArgumentException("no user"));

        var cart = orderRepository.findCartByUser(inDatabase).orElseThrow(() -> new IllegalArgumentException("no cart"));
        if (cart.getOrderContents().isEmpty()) {
            throw new IllegalArgumentException("cart is empty");
        }

        orderRepository.setAddressForOrder(cart.getId(), request.getZip(), request.getCity(), request.getStreet(), request.getStreetnumber());
        return ResponseEntity.ok(new MessageResponse("okay"));
    }

}
