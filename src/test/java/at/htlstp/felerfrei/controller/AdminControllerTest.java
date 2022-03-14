package at.htlstp.felerfrei.controller;

import at.htlstp.felerfrei.FelerfreibackendApplication;
import at.htlstp.felerfrei.domain.Role;
import at.htlstp.felerfrei.domain.RoleAuthority;
import at.htlstp.felerfrei.domain.user.User;
import at.htlstp.felerfrei.persistence.ProductRepository;
import at.htlstp.felerfrei.persistence.RoleRepository;
import at.htlstp.felerfrei.persistence.UserRepository;
import at.htlstp.felerfrei.persistence.VerificationTokenRepository;
import org.json.JSONObject;
import org.junit.After;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
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

    private User testAdmin;

    @BeforeEach
    void setup() {
        var role = new Role(null, RoleAuthority.ROLE_ADMIN);
        roleRepository.save(role);
        var user = new User(1, "Firstname", "Lastname", "my@email.com",
                encoder.encode("password"), true, null, role);
        user.setRole(role);
        testAdmin = userRepository.save(user);
    }

    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
    }

    String login() throws Exception {
        var response =  mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "email": "my@email.com",
                            "password": "password"
                        }
                        """)).andReturn();
        var json = new JSONObject(response.getResponse().getContentAsString());
        return json.getString("token");
    }

    @Test
    void signup_works() throws Exception {
        var token = login();
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
        assertThat(productRepository.findAll().size()).isEqualTo(1);
    }
}
