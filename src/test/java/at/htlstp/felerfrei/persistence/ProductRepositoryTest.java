package at.htlstp.felerfrei.persistence;

import at.htlstp.felerfrei.FelerfreibackendApplication;
import at.htlstp.felerfrei.domain.Image;
import at.htlstp.felerfrei.domain.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = FelerfreibackendApplication.class
)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
class ProductRepositoryTest {
    @Autowired
    private ProductRepository productRepository;

    @AfterEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    void many_to_many_images() {
        var images = List.of(
                new Image(null, "path1"),
                new Image(null, "path2"),
                new Image(null, "path3")
        );

        var product = new Product(null, "name", "description", true, 23D,null, images);
        var saved = productRepository.save(product);

        assertThat(saved.getImages()).hasSize(3);
    }

    @Test
    void set_images_afterwards() {
        var images = List.of(
                new Image(null, "path1"),
                new Image(null, "path2"),
                new Image(null, "path3")
        );

        var product = new Product(null, "name", "description", true, 23D, null, null);
        product.addAllImages(images);

        var saved = productRepository.save(product);

        assertThat(saved.getImages()).hasSize(3);
    }
}