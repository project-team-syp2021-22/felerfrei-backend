package at.htlstp.felerfrei.controller;

import at.htlstp.felerfrei.domain.RoleAuthority;
import at.htlstp.felerfrei.domain.user.User;
import at.htlstp.felerfrei.domain.user.VerificationToken;
import at.htlstp.felerfrei.payload.request.*;
import at.htlstp.felerfrei.payload.response.JwtResponse;
import at.htlstp.felerfrei.payload.response.MessageResponse;
import at.htlstp.felerfrei.persistence.RoleRepository;
import at.htlstp.felerfrei.persistence.UserRepository;
import at.htlstp.felerfrei.persistence.VerificationTokenRepository;
import at.htlstp.felerfrei.security.jwt.JwtUtils;
import at.htlstp.felerfrei.security.services.UserDetailsImpl;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.mail.Message;
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

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository,
                          VerificationTokenRepository verificationTokenRepository, RoleRepository roleRepository,
                          PasswordEncoder encoder, JwtUtils jwtUtils, MailSender mailSender) {
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
        if (!passwordIsValid(signUpRequest.getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new MessageResponse("Passwort entspricht nicht den empfohlenen Vorgaben. Bitte verwenden Sie mindestens 8 Zeichen, einen Großbuchstaben, einen Kleinbuchstaben, eine Zahl und ein Sonderzeichen."));
        }
        User user;
        try {
            user = new User(signUpRequest.getFirstname(), signUpRequest.getLastname(), signUpRequest.getEmail(),
                    encoder.encode(signUpRequest.getPassword()), signUpRequest.getTelephone());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

        var role = roleRepository.findByName(RoleAuthority.ROLE_USER);
        user.setRole(role.orElseThrow(() -> new NoSuchElementException("Role not found")));

        var saved = userRepository.save(user);
        var token = UUID.randomUUID().toString();
        var savedToken = verificationTokenRepository.save(new VerificationToken(token, saved));

        mailSender.sendVerificationEmail(savedToken, "http://localhost:3000/verify/");

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyAccount(@RequestBody TokenRequest token) {
        var found = verificationTokenRepository.findByToken(token.getToken());
        if (found.isPresent()) {
            var verificationToken = found.get();
            if (LocalDateTime.now().isAfter(verificationToken.getExpiryDate())) {
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

    @PostMapping("/changeCredentials")
    public ResponseEntity<JwtResponse> changeCredential(@Valid @RequestBody ChangeCredentialsRequest request) {
        var email = jwtUtils.getEmailFromToken(request.getToken());
        var user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (request.getPassword() == null)
            throw new BadCredentialsException("Password cannot be null");
        if (!encoder.matches(request.getPassword(), user.getPassword()))
            throw new BadCredentialsException("Wrong password");

        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new BadCredentialsException("Email already in use");
        }
        user.setEmail(request.getEmail());
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setTelephonenumber(request.getTelephone());
        user.setPassword(encoder.encode(request.getPassword())); // muss man machen weil salt anders ist

        userRepository.save(user);

        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateToken(authentication);
        var userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return ResponseEntity
                .ok(new JwtResponse(jwt, userDetails.getUsername(), user.getFirstname(), user.getLastname(), user.getTelephonenumber()));
    }

    @PostMapping("/requestResetPassword")
    public ResponseEntity<MessageResponse> requestPasswordReset(@Valid @RequestBody ResetRequest request) {
        var user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        var token = UUID.randomUUID().toString();
        var savedToken = verificationTokenRepository.save(new VerificationToken(token, user));
        mailSender.sendPasswordResetEmail(savedToken, "http://localhost:3000/reset/");
        return ResponseEntity.ok(new MessageResponse("Password reset email sent!"));
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        var found = verificationTokenRepository.findByToken(request.getToken());
        if (found.isPresent()) {
            var verificationToken = found.get();
            if (LocalDateTime.now().isAfter(verificationToken.getExpiryDate())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Verification token expired!"));
            }
            if (!passwordIsValid(request.getNewPassword())) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(new MessageResponse("Passwort entspricht nicht den empfohlenen Vorgaben. Bitte verwenden Sie mindestens 8 Zeichen, einen Großbuchstaben, einen Kleinbuchstaben, eine Zahl und ein Sonderzeichen."));
            }
            var user = verificationToken.getUser();
            user.setPassword(encoder.encode(request.getNewPassword()));
            userRepository.save(user);
            verificationTokenRepository.delete(verificationToken);
            return ResponseEntity.ok(new MessageResponse("Password reset!"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Invalid token"));
        }
    }

    private boolean passwordIsValid(String password) {
        if (password == null) {
            return false;
        }
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    }
}
