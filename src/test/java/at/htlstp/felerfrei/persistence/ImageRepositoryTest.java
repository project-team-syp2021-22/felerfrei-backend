package at.htlstp.felerfrei.persistence;

import at.htlstp.felerfrei.FelerfreibackendApplication;
import at.htlstp.felerfrei.domain.Image;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = FelerfreibackendApplication.class
)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
class ImageRepositoryTest {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        imageRepository.deleteAll();
    }

    @Test
    void find_by_id() {
        var savedImage = imageRepository.save(new Image(null, "path"));
        var image = imageRepository.findById(savedImage.getId()).orElseThrow();
        assertEquals("path", image.getPath());
    }

    @Test
    void findAll() {
        imageRepository.save(new Image(null, "path"));
        imageRepository.save(new Image(null, "path"));
        assertEquals(2, imageRepository.findAll().size());
    }

    @Test
    void save() {
        imageRepository.save(new Image(null, "path"));
        assertEquals(1, imageRepository.findAll().size());
    }

    @Test
    void save_throws() {
        assertThrows(Exception.class, () -> {
            imageRepository.save(new Image(null, null));
        });
    }

    @Test
    void delete() {
        var savedImage = imageRepository.save(new Image(null, "path"));
        imageRepository.delete(savedImage);
        assertEquals(0, imageRepository.findAll().size());
    }
}