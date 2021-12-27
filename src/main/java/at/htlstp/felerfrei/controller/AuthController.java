package at.htlstp.felerfrei.controller;

import at.htlstp.felerfrei.domain.RoleAuthority;
import at.htlstp.felerfrei.domain.User;
import at.htlstp.felerfrei.payload.request.SignupRequest;
import at.htlstp.felerfrei.payload.response.JwtResponse;
import at.htlstp.felerfrei.payload.request.LoginRequest;
import at.htlstp.felerfrei.payload.response.MessageResponse;
import at.htlstp.felerfrei.persistence.RoleRepository;
import at.htlstp.felerfrei.persistence.UserRepository;
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
import java.util.NoSuchElementException;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder encoder;

    private final JwtUtils jwtUtils;

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder encoder, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateToken(authentication);
        var userDetails = (UserDetailsImpl) authentication.getPrincipal();
        var user = userRepository.findById(userDetails.getId()).orElseThrow();
        return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getUsername(), user.getFirstname(), user.getLastname()));
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
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

}
