package com.backend.SecureSharing.controller;

import com.backend.SecureSharing.entity.User;
import com.backend.SecureSharing.jwt.AuthRequest;
import com.backend.SecureSharing.jwt.AuthResponse;
import com.backend.SecureSharing.jwt.RefreshTokenStore;
import com.backend.SecureSharing.service.JwtService;
import com.backend.SecureSharing.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin("http://localhost:1234")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/register")
    public String register(@RequestBody User user){
        User saved = userService.register(user);
        if(saved!=null){
            return user.getName()+" registered successfully";
        }
        return "Failed to register "+user.getName();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@RequestBody AuthRequest authRequest){
        return ResponseEntity.ok(userService.loginUser(authRequest));
    }
}
