package com.backend.SecureSharing.service;

import com.backend.SecureSharing.DTO.ProfileDTO;
import com.backend.SecureSharing.entity.User;
import com.backend.SecureSharing.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ProfileService {

    @Autowired
    UserRepository userRepository;

    public ProfileDTO updateProfile(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(()->new RuntimeException("User not found"));

        return new ProfileDTO(
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                LocalDateTime.now(),
                "Active"
        );
    }
}
