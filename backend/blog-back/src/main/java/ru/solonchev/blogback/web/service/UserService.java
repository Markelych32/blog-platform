package ru.solonchev.blogback.web.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.solonchev.blogback.persistence.model.User;
import ru.solonchev.blogback.persistence.repository.UserRepository;
import ru.solonchev.blogback.web.dto.UserProfileDto;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
    }

    public void addUser(User user) {
        userRepository.save(user);
    }

    public boolean isExistsByEmail(String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }

    public UserProfileDto getUserProfileById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        return UserProfileDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}
