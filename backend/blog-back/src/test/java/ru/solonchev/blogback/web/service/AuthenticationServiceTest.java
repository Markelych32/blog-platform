package ru.solonchev.blogback.web.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UserDetails userDetails;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthenticationService authenticationService;

    private final String email = "test@example.com";
    private final String password = "password";
    private final String token = "test.jwt.token";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authenticationService, "secretKey", "testSecretKeyWithLength32Characters1234567890");
    }

    @Test
    @DisplayName("Should authenticate user with valid credentials")
    void shouldAuthenticateUserWithValidCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        UserDetails result = authenticationService.authenticate(email, password);

        assertNotNull(result);
        assertEquals(userDetails, result);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(authentication).isAuthenticated();
        verify(authentication).getPrincipal();
    }

    @Test
    @DisplayName("Should throw BadCredentialsException when authentication fails")
    void shouldThrowBadCredentialsExceptionWhenAuthenticationFails() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authenticationService.authenticate(email, password));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Should throw BadCredentialsException when authentication is not successful")
    void shouldThrowBadCredentialsExceptionWhenAuthenticationIsNotSuccessful() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authenticationService.authenticate(email, password));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(authentication).isAuthenticated();
    }

    @Test
    @DisplayName("Should generate token for authenticated user")
    void shouldGenerateTokenForAuthenticatedUser() {
        when(userDetails.getUsername()).thenReturn(email);

        String generatedToken = authenticationService.generateToken(userDetails);

        assertNotNull(generatedToken);
        assertTrue(generatedToken.length() > 0);

        verify(userDetails).getUsername();
    }

    @Test
    @DisplayName("Should validate token and return user details")
    void shouldValidateTokenAndReturnUserDetails() {
        when(userDetails.getUsername()).thenReturn(email);
        String validToken = authenticationService.generateToken(userDetails);

        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);

        UserDetails result = authenticationService.validateToken(validToken);

        assertNotNull(result);
        assertEquals(userDetails, result);

        verify(userDetailsService).loadUserByUsername(email);
    }
}