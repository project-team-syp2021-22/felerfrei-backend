package at.htlstp.felerfrei.controller;

import at.htlstp.felerfrei.domain.RoleAuthority;
import at.htlstp.felerfrei.domain.user.User;
import at.htlstp.felerfrei.domain.user.VerificationToken;
import at.htlstp.felerfrei.payload.request.SignupRequest;
import at.htlstp.felerfrei.payload.request.TokenRequest;
import at.htlstp.felerfrei.payload.response.JwtResponse;
import at.htlstp.felerfrei.payload.request.LoginRequest;
import at.htlstp.felerfrei.payload.response.MessageResponse;
import at.htlstp.felerfrei.persistence.RoleRepository;
import at.htlstp.felerfrei.persistence.UserRepository;
import at.htlstp.felerfrei.persistence.VerificationTokenRepository;
import at.htlstp.felerfrei.security.jwt.JwtUtils;
import at.htlstp.felerfrei.security.services.UserDetailsImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final VerificationTokenRepository verificationTokenRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder encoder;

    private final JwtUtils jwtUtils;

    private final MailSender mailSender;

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, VerificationTokenRepository verificationTokenRepository, RoleRepository roleRepository, PasswordEncoder encoder, JwtUtils jwtUtils, MailSender mailSender) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
        this.mailSender = mailSender;
    }

    @PostMapping("/check")
    public ResponseEntity<MessageResponse> checkUser(@RequestBody TokenRequest token) {
        try {
            jwtUtils.getEmailFromToken(token.getToken());
        } catch (RuntimeException e) {
            return new ResponseEntity<>(new MessageResponse("Invalid token"), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(new MessageResponse("Valid token"), HttpStatus.OK);
    }


    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateToken(authentication);
        var userDetails = (UserDetailsImpl) authentication.getPrincipal();
        var user = userRepository.findById(userDetails.getId()).orElseThrow();
        return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getUsername(), user.getFirstname(), user.getLastname(), user.getTelephonenumber()));
    }

    @PostMapping("/signup")
    public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        var user = new User(signUpRequest.getFirstname(), signUpRequest.getLastname(), signUpRequest.getEmail(), encoder.encode(signUpRequest.getPassword()), signUpRequest.getTelephone());
        var role = roleRepository.findByName(RoleAuthority.ROLE_USER);
        try {
            user.setRole(role.orElseThrow(() -> new NoSuchElementException("Role not found")));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Could not register User!"));
        }
        var saved = userRepository.save(user);
        var token = UUID.randomUUID().toString();
        verificationTokenRepository.save(new VerificationToken(token, saved));

        mailSender.sendVerificationEmail(saved, "http://localhost:3000/verify/");

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyAccount(@RequestBody TokenRequest token) {
        var found = verificationTokenRepository.findByToken(token.getToken());
        if (found.isPresent()) {
            var verificationToken = found.get();
            if(LocalDateTime.now().isAfter(verificationToken.getExpiryDate())) {
                return ResponseEntity.badRequest().body("Verification token expired!");
            }
            var user = verificationToken.getUser();
            user.setEnabled(true);
            userRepository.save(user);
            verificationTokenRepository.delete(verificationToken);
            return ResponseEntity.ok("Account verified!");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid token");
        }
    }
}
