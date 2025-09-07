package com.backend.SecureSharing.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileDTO {
    private String name;
    private String email;
    private LocalDateTime memberSince;
    private LocalDateTime lastLogin;
    private String accountStatus;
}
