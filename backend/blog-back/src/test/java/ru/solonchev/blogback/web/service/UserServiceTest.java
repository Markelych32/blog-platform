package ru.solonchev.blogback.web.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.solonchev.blogback.persistence.model.User;
import ru.solonchev.blogback.persistence.repository.UserRepository;
import ru.solonchev.blogback.web.dto.UserProfileDto;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UUID userId;
    private User user;
    private UserProfileDto userProfileDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        user = new User()
                .setId(userId)
                .setName("Test User")
                .setEmail("test@example.com")
                .setPassword("password")
                .setCreatedAt(LocalDateTime.now());

        userProfileDto = UserProfileDto.builder()
                .id(userId)
                .name("Test User")
                .email("test@example.com")
                .build();
    }

    @Test
    @DisplayName("Should find user by id when findUserById is called with valid id")
    void shouldFindUserByIdWhenFindUserByIdIsCalledWithValidId() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.findUserById(userId);

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getEmail(), result.getEmail());

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when findUserById is called with non-existing id")
    void shouldThrowEntityNotFoundExceptionWhenFindUserByIdIsCalledWithNonExistingId() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> userService.findUserById(userId)
        );

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should add user when addUser is called")
    void shouldAddUserWhenAddUserIsCalled() {
        when(userRepository.save(user)).thenReturn(user);

        userService.addUser(user);

        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should check if user exists by email when isExistsByEmail is called")
    void shouldCheckIfUserExistsByEmailWhenIsExistsByEmailIsCalled() {
        String email = "test@example.com";
        when(userRepository.existsByEmailIgnoreCase(email)).thenReturn(true);

        boolean result = userService.isExistsByEmail(email);

        assertTrue(result);
        verify(userRepository).existsByEmailIgnoreCase(email);
    }

    @Test
    @DisplayName("Should get user profile by id when getUserProfileById is called with valid id")
    void shouldGetUserProfileByIdWhenGetUserProfileByIdIsCalledWithValidId() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserProfileDto result = userService.getUserProfileById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getEmail(), result.getEmail());

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when getUserProfileById is called with non-existing id")
    void shouldThrowEntityNotFoundExceptionWhenGetUserProfileByIdIsCalledWithNonExistingId() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> userService.getUserProfileById(userId)
        );

        verify(userRepository).findById(userId);
    }
}