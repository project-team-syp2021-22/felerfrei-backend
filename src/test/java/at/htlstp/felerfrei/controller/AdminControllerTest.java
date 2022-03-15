package at.htlstp.felerfrei.controller;

import at.htlstp.felerfrei.FelerfreibackendApplication;
import at.htlstp.felerfrei.domain.Role;
import at.htlstp.felerfrei.domain.RoleAuthority;
import at.htlstp.felerfrei.domain.user.User;
import at.htlstp.felerfrei.persistence.ProductRepository;
import at.htlstp.felerfrei.persistence.RoleRepository;
import at.htlstp.felerfrei.persistence.UserRepository;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = FelerfreibackendApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
public class AdminControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder encoder;

    @BeforeEach
    void setup() {
        var role = new Role(null, RoleAuthority.ROLE_ADMIN);
        roleRepository.save(role);
        var user = new User(1, "Firstname", "Lastname", "admin@email.com",
                encoder.encode("password"), true, null, role);
        user.setRole(role);
        userRepository.save(user);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        productRepository.deleteAll();
    }

    String login(String email) throws Exception {
        var response = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {
                            "email": "%s",
                            "password": "password"
                        }
                        """, email))).andReturn();
        var json = new JSONObject(response.getResponse().getContentAsString());
        return json.getString("token");
    }

    @Nested
    class AddProductTest {

        @Test
        void addProduct_works() throws Exception {
            var length = productRepository.findAll().size();

            var token = login("admin@email.com");
            mvc.perform(post("/admin/addProduct")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + token)
                            .content("""
                                       {
                                            "name" : "test",
                                            "description"  :"description",
                                            "price" : 12.99,
                                            "material" : "Eiche"
                                       }
                                    """))
                    .andDo(print())
                    .andExpect(status().isOk());
            assertThat(productRepository.findAll().size()).isGreaterThan(length);
        }

        @Test
        void addProduct_null_values() throws Exception {
            var token = login("admin@email.com");
            mvc.perform(post("/admin/addProduct")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + token)
                            .content("""
                                       {
                                            "name" : null,
                                            "description"  :null,
                                            "price" : null,
                                            "material" : null
                                       }
                                    """))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
            assertThat(productRepository.findAll().size()).isZero();
        }

        @Test
        void no_admin_request() throws Exception {
            var role = new Role(null, RoleAuthority.ROLE_USER);
            roleRepository.save(role);
            var user = new User(1, "Firstname", "Lastname", "user@email.com",
                    encoder.encode("password"), true, null, role);
            user.setRole(role);
            userRepository.save(user);

            var token = login("user@email.com");

            mvc.perform(post("/admin/addProduct")
                     .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .content("""
                                {
                                    "name" : "test",
                                    "description"  :"description",
                                    "price" : 12.99,
                                    "material" : "Eiche"
                                }
                            """)).andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        void no_token() throws Exception {
            mvc.perform(post("/admin/addProduct")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "name" : "test",
                                    "description"  :"description",
                                    "price" : 12.99,
                                    "material" : "Eiche"
                                }
                            """)).andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class GetProductsTest {

        @Test
        void get_products_works() throws Exception {
            var token = login("admin@email.com");

            mvc.perform(get("/admin/products?size=1&page=0")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + token))
                            .andDo(print())
                            .andExpect(status().isOk());
        }

        @Test
        void get_products_unauthorized() throws Exception {
            mvc.perform(get("/admin/products?size=1&page=0"))
                            .andDo(print())
                            .andExpect(status().isUnauthorized());
        }

        @Test
        void get_products_forbidden() throws Exception {
            var role = new Role(null, RoleAuthority.ROLE_USER);
            roleRepository.save(role);
            var user = new User(1, "Firstname", "Lastname", "user@email.com",
                    encoder.encode("password"), true, null, role);
            user.setRole(role);
            userRepository.save(user);

            mvc.perform(get("/admin/products?size=1&page=0")
                            .header("Authorization", "Bearer " + login("user@email.com")))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

    }
}
