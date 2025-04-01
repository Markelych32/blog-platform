package ru.solonchev.blogback.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.solonchev.blogback.web.dto.AuthResponse;
import ru.solonchev.blogback.web.dto.LoginRequest;
import ru.solonchev.blogback.web.service.AuthenticationService;

@RestController
@RequestMapping(path = "/api/v1/auth/login")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping
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
}
