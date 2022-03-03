package at.htlstp.felerfrei.persistence;

import at.htlstp.felerfrei.FelerfreibackendApplication;
import at.htlstp.felerfrei.domain.Product;
import at.htlstp.felerfrei.domain.Role;
import at.htlstp.felerfrei.domain.RoleAuthority;
import at.htlstp.felerfrei.domain.user.User;
import at.htlstp.felerfrei.domain.order.Order;
import at.htlstp.felerfrei.domain.order.OrderContent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = FelerfreibackendApplication.class
)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @AfterEach
    void setup() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    void save_works() {
        var role = roleRepository.save(new Role(null, RoleAuthority.ROLE_USER));
        var user = userRepository.save(new User(null, "Florian", "Hugo", "f.kainrath@hugo.com", "", true, null, role));
        var product = productRepository.save(new Product(null, "Sessel", "", true, 23D,null, null));
        var order = new Order(1, LocalDate.now(), true, "test", user, null);
        var inhalt = List.of(
                new OrderContent(1, 2, null, 24.99, order, product),
                new OrderContent(2, 1, "Mit Schriftzug", 24.99, order, product)
        );
        order.setOrderContent(inhalt);
        var saved = orderRepository.save(order);
        var found = orderRepository.findById(saved.getId()).orElseThrow();

        assertThat(saved.getOrderContents()).containsAll(found.getOrderContents());
    }

}