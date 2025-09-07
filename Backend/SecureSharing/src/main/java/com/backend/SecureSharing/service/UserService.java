package com.backend.SecureSharing.service;

import com.backend.SecureSharing.entity.User;
import com.backend.SecureSharing.jwt.AuthRequest;
import com.backend.SecureSharing.jwt.AuthResponse;
import com.backend.SecureSharing.jwt.RefreshTokenStore;
import com.backend.SecureSharing.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    JwtService jwtService;
    @Autowired
    RefreshTokenStore refreshTokenStore;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserRepository userRepository;

    public User register(User user) {
        if(user.getName()==null || user.getEmail()==null || user.getPassword()==null){
            throw new NullPointerException("Enter all the fields");
        }else {
            String password = passwordEncoder.encode(user.getPassword());
            user.setPassword(password);
            user.setCreatedAt(LocalDateTime.now());
            return userRepository.save(user);
        }
    }

    public AuthResponse loginUser(AuthRequest authRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getEmail() , authRequest.getPassword()));
        String accessToken = jwtService.generateAccessToken(authRequest.getEmail());
        String refreshToken = jwtService.generateRefreshToken(authRequest.getEmail());

        refreshTokenStore.save(authRequest.getEmail(),refreshToken);
        return new AuthResponse(accessToken,refreshToken);
    }

}
