package ru.solonchev.blogback.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.solonchev.blogback.security.BlogUserDetails;
import ru.solonchev.blogback.web.dto.AuthResponse;
import ru.solonchev.blogback.web.dto.LoginRequest;
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
}
