package at.htlstp.felerfrei.controller;

import at.htlstp.felerfrei.FelerfreibackendApplication;
import at.htlstp.felerfrei.domain.Product;
import at.htlstp.felerfrei.persistence.ProductRepository;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = FelerfreibackendApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
public class DataControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    public void setUp() {
        productRepository.deleteAll();
        productRepository.save(new Product(null, "Tisch", "", true, 13.99, null));
        productRepository.save(new Product(null, "Stuhl", "", true, 23.99, null));
        productRepository.save(new Product(null, "Schrank", "", true, 33.99, null));
        productRepository.save(new Product(null, "Bett", "", false, 43.99, null));
    }

    @Test
    public void pageable_products() throws Exception {
        var result = mvc.perform(get("/api/products").param("size", "1"))
                .andDo(print())
                .andReturn();
        var jsonObject = new JSONObject(result.getResponse().getContentAsString());
        var content = jsonObject.getJSONArray("content");
        assertEquals(1, content.length());
    }

    @Test
    public void only_contains_publisched_products() throws Exception {
        var result = mvc.perform(get("/api/products?size=4"))
                .andDo(print())
                .andReturn();
        var jsonObject = new JSONObject(result.getResponse().getContentAsString());
        var content = jsonObject.getJSONArray("content");
        assert content.length() == 3;
    }
}
