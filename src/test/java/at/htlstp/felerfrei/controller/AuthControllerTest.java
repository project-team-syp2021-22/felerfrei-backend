package at.htlstp.felerfrei.controller;

import at.htlstp.felerfrei.FelerfreibackendApplication;
import at.htlstp.felerfrei.domain.Role;
import at.htlstp.felerfrei.domain.RoleAuthority;
import at.htlstp.felerfrei.domain.user.User;
import at.htlstp.felerfrei.domain.user.VerificationToken;
import at.htlstp.felerfrei.payload.request.LoginRequest;
import at.htlstp.felerfrei.persistence.RoleRepository;
import at.htlstp.felerfrei.persistence.UserRepository;
import at.htlstp.felerfrei.persistence.VerificationTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = FelerfreibackendApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    private User testUser;

    @BeforeEach
    void setup() {
        var role = new Role(null, RoleAuthority.ROLE_USER);
        roleRepository.save(role);
        var user = new User(1, "Firstname", "Lastname", "my@email.com",
                encoder.encode("password"), true, null, role);
        user.setRole(role);
        testUser = userRepository.save(user);
    }

    @AfterEach
    void tearDown() {
        verificationTokenRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nested
    class SignUpTest {
        @Test
        void test_signup_null_email() throws Exception {
            mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON).content("""
                                {
                                    "email": null,
                                    "password": "test"
                                    "firstName": "test",
                                    "lastName": "test"
                                }
                            """))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("null value in request"));
        }

        @Test
        void signup_null_password() throws Exception {
            mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content("""
                               {
                                   "firstname"  :"Hugo",
                                   "lastname" : "Meier",
                                   "email" : "test@test.com",
                                   "telephone" : null
                               }
                            """))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }

        @Test
        void signup_works() throws Exception {
            mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content("""
                               {
                                    "password" : "password1!A",
                                    "firstname"  :"Hugo",
                                    "lastname" : "Meier",
                                    "email" : "test@test.com",
                                    "telephone" : "null"
                               }
                            """))
                    .andDo(print())
                    .andExpect(status().isOk());
            assertThat(userRepository.findByEmail("test@test.com")).isPresent();
        }

        @Test
        void signup_already_exists() throws Exception {
            mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content("""
                            {
                                 "password" : "password",
                                 "firstname"  :"Hugo",
                                 "lastname" : "Meier",
                                 "email" : "my@email.com"
                            }
                                 """))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class LoginTest {
        @Test
        void test_password_is_null_login() throws Exception {
            mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                                {
                                    "email": "test@testasdf.com",
                                    "password": null
                                }
                            """))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        void login_works() throws Exception {
            mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                            {
                                "email" : "my@email.com",
                                "password" : "password"
                            }
                            """))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        void login_with_null_email() throws Exception {
            mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                            {
                                "email" : null,
                                "password" : "test"
                            }
                            """))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("null value in request"));
        }

        @Test
        void login_with_no_such_email() throws Exception {
            mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(new LoginRequest("does@not.exist", "test")))
                            .accept(MediaType.APPLICATION_JSON)).andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Bad credentials"));
        }

    }

    @Nested
    class ChangeCredentialsTest {

        @Test
        void change_credentials_bad_credentials() throws Exception {
            var loginResponse = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "email" : "my@email.com",
                                        "password" : "password"
                                    }
                                    """))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andReturn();
            var token = new JSONObject(loginResponse.getResponse().getContentAsString()).getString("token");

            mvc.perform(post("/auth/changeCredentials").contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("""
                                    {
                                        "token" : "%s",
                                        "email" : "new@email.com",
                                        "password" : "falsePassword",
                                        "firstname" : "Hugo",
                                        "lastname" : "Meier"
                                    }
                                    """, token)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Wrong password"));
            assertThat(userRepository.findById(testUser.getId()).get().getEmail())
                    .isEqualTo("my@email.com");
        }

        @Test
        void change_credentials_works() throws Exception {
            var response = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                    {
                        "email" : "my@email.com",
                        "password" : "password"
                    }
                    """)).andReturn();
            var wrapped = new JSONObject(response.getResponse().getContentAsString());
            var token = wrapped.getString("token");

            mvc.perform(post("/auth/changeCredentials").contentType(MediaType.APPLICATION_JSON).content(String.format("""
                    {
                        "token" : "%s",
                        "email" : "my@email.com",
                        "password" : "password",
                        "firstname" : "Hugo",
                        "lastname" : "Meier",
                        "telephone" : "0123456789"
                    }
                    """, token))).andDo(print()).andExpect(status().isOk());

            assertThat(userRepository.findByEmail(testUser.getEmail()).get().getTelephonenumber()).isNotNull();
        }

        @Test
        void change_credentials_no_email_present() throws Exception {
            var response = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                    {
                        "email" : "my@email.com",
                        "password" : "password"
                    }
                    """)).andReturn();
            var wrapped = new JSONObject(response.getResponse().getContentAsString());
            var token = wrapped.getString("token");

            mvc.perform(post("/auth/changeCredentials").contentType(MediaType.APPLICATION_JSON).content(String.format("""
                    {
                        "token" : "%s",
                        "password" : "password",
                        "firstname" : "Hugo",
                        "lastname" : "Meier",
                        "telephone" : "0123456789"
                    }
                    """, token))).andDo(print()).andExpect(status().isBadRequest());
            assertThat(userRepository.findByEmail("my@email.com").get().getTelephonenumber()).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"not.a.token", "", "jfks.adl.jf"})
        void change_credentials_invalid_tokens(String token) throws Exception {
            mvc.perform(post("/auth/changeCredentials").contentType(MediaType.APPLICATION_JSON).content(String.format("""
                    {
                        "token" : "%s",
                        "email" : "my@email.com",
                        "password" : "password",
                        "firstname" : "Hugo",
                        "lastname" : "Meier",
                        "telephone" : "0123456789"
                    }
                    """, token))).andDo(print()).andExpect(status().isBadRequest());
            assertThat(userRepository.findByEmail("my@email.com").get().getTelephonenumber()).isNull();
        }
    }

    @Nested
    class VerificationTest {
        @Test
        void verification_works() throws Exception {
            var role = roleRepository.save(new Role(null, RoleAuthority.ROLE_USER));
            var user = new User("Test", "lastname", "my@mail.com", encoder.encode("password"), "0123456789");
            user.setRole(role);
            var verificationToken = new VerificationToken(UUID.randomUUID().toString(), userRepository.save(user));
            verificationTokenRepository.save(verificationToken);

            mvc.perform(post("/auth/verify").contentType(MediaType.APPLICATION_JSON).content(String.format("""
                    {
                        "token" : "%s"
                    }
                    """, verificationToken.getToken()))).andDo(print()).andExpect(status().isOk());

            assertThat(userRepository.findByEmail("my@mail.com").get().getEnabled()).isTrue();
        }

        @Test
        void verification_token_expired() throws Exception {
            var role = roleRepository.save(new Role(null, RoleAuthority.ROLE_USER));
            var user = new User("Test", "lastname", "my@mail.com", encoder.encode("password"), "0123456789");
            user.setRole(role);
            var verificationToken = new VerificationToken(UUID.randomUUID().toString(), userRepository.save(user));
            verificationToken.setExpiryDate(LocalDateTime.now().minusDays(1));
            verificationTokenRepository.save(verificationToken);

            mvc.perform(post("/auth/verify").contentType(MediaType.APPLICATION_JSON).content(String.format("""
                    {
                        "token" : "%s"
                    }
                    """, verificationToken.getToken()))).andDo(print()).andExpect(status().isBadRequest());

            assertThat(userRepository.findByEmail("my@mail.com").get().getEnabled()).isFalse();
        }

        @Test
        void verification_token_does_not_exist() throws Exception {
            var role = roleRepository.save(new Role(null, RoleAuthority.ROLE_USER));
            var user = new User("Test", "lastname", "my@mail.com", encoder.encode("password"), "0123456789");
            user.setRole(role);
            userRepository.save(user);

            mvc.perform(post("/auth/verify").contentType(MediaType.APPLICATION_JSON).content(String.format("""
                    {
                        "token" : "%s"
                    }
                    """, "i-do-not-exist"))).andDo(print()).andExpect(status().isNotFound());

            assertThat(userRepository.findByEmail("my@mail.com").get().getEnabled()).isFalse();
        }
    }

    @Nested
    class ResetPasswordTest {
        @Test
        void request_reset_password_bad_request() throws Exception {
            mvc.perform(post("/auth/requestResetPassword")
                            .contentType(MediaType.APPLICATION_JSON).content("""
                                    {
                                        "email" : null,
                                    }
                                    """))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        void request_reset_password_works() throws Exception {

            // request password reset
            mvc.perform(post("/auth/requestResetPassword").contentType(MediaType.APPLICATION_JSON).content("""
                            {
                                "email" : "my@email.com"
                            }
                            """))
                    .andDo(print())
                    .andExpect(status().isOk());

            var inDatabase = verificationTokenRepository.findByUser(testUser);
            // reset password
            mvc.perform(post("/auth/resetPassword").contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("""
                                                {
                                                    "token" : "%s",
                                                    "newPassword" : "newPassword1!A"
                                                }                
                                    """, inDatabase.getToken())))
                    .andDo(print())
                    .andExpect(status().isOk());
            // check if password has been changed
            mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                       "email" : "my@email.com",
                                       "password" : "newPassword1!A"
                                    }
                                    """))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        void reset_invalid_token() throws Exception {
            mvc.perform(post("/auth/resetPassword").contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("""
                                                {
                                                    "token" : "%s",
                                                    "newPassword" : "newPassword"
                                                }                  
                                    """, "i-do-not-exist")))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        void reset_token_expired() throws Exception {


            mvc.perform(post("/auth/requestResetPassword")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(("""
                                    {
                                        "email" : "my@email.com"
                                    }
                                    """)))
                    .andDo(print())
                    .andExpect(status().isOk());

            var vToken = verificationTokenRepository.findByUser(testUser);
            vToken.setExpiryDate(LocalDateTime.now().minusDays(1));
            verificationTokenRepository.save(vToken);

            mvc.perform(post("/auth/resetPassword").contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("""
                                        {
                                            "token" : "%s",
                                            "newPassword" : "newPassword"
                                        }
                                    """, vToken.getToken())))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class CheckTokenTest {

        @Test
        void works() throws Exception {
            var response = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                       "email" : "my@email.com",
                                       "password" : "password"
                                    }
                                    """))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andReturn();

            var token = new JSONObject(response.getResponse().getContentAsString()).getString("token");

            response = mvc.perform(post("/auth/check").contentType(MediaType.APPLICATION_JSON)
                    .content(String.format("""
                            {
                                "token" : "%s"
                            }
                            """, token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andReturn();
            var message = new JSONObject(response.getResponse().getContentAsString()).getString("message");
            assertEquals("Valid token", message);
        }

        @Test
        void invalid_token() throws Exception {
            var response = mvc.perform(post("/auth/check").contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "token" : "id-do-not-exist"
                            }
                            """))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andReturn();
            var message = new JSONObject(response.getResponse().getContentAsString()).getString("message");
            assertEquals("Invalid token", message);
        }

        @Test
        void null_request() throws Exception {
            mvc.perform(post("/auth/check").contentType(MediaType.APPLICATION_JSON)
                    .content(""))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

        }
    }
}
