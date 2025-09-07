package com.backend.SecureSharing.controller;

import com.backend.SecureSharing.DTO.ProfileDTO;
import com.backend.SecureSharing.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin("http://localhost:1234")
public class ProfileController {
    @Autowired
    ProfileService profileService;

    @GetMapping("/profile")
    public ResponseEntity<ProfileDTO> updateProfile(Authentication authentication){
        return ResponseEntity.ok(profileService.updateProfile(authentication));
    }
}
