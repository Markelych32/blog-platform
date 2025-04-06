package ru.solonchev.blogback.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.solonchev.blogback.persistence.model.User;
import ru.solonchev.blogback.security.BlogUserDetails;
import ru.solonchev.blogback.web.dto.AuthResponse;
import ru.solonchev.blogback.web.dto.LoginRequest;
import ru.solonchev.blogback.web.dto.SignupRequestDto;
import ru.solonchev.blogback.web.dto.UserProfileDto;
import ru.solonchev.blogback.web.service.AuthenticationService;
import ru.solonchev.blogback.web.service.UserService;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        UserDetails userDetails = authenticationService.authenticate(
                request.getEmail(),
                request.getPassword()
        );
        String token = authenticationService.generateToken(userDetails);
        AuthResponse authResponse = new AuthResponse()
                .setToken(token)
                .setExpiresIn(86400);
        return ResponseEntity.ok(authResponse);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof BlogUserDetails userDetails) {
            UUID userId = userDetails.getId();
            UserProfileDto userProfile = userService.getUserProfileById(userId);
            return ResponseEntity.ok(userProfile);
        }
        return ResponseEntity.status(401).build();
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequestDto requestDto) {
        if (userService.isExistsByEmail(requestDto.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Choose another email");
        }
        String hashedPassword = passwordEncoder.encode(requestDto.getPassword());
        User user = new User()
                .setName(requestDto.getName())
                .setPassword(hashedPassword)
                .setEmail(requestDto.getEmail());
        userService.addUser(user);
        return ResponseEntity.ok("Success, Baby");
    }
}
