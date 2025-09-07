package com.backend.SecureSharing.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.Instant;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShareLink {
    @Id
    private String id;

    private String fileId;          // Reference to uploaded file
    private String token;           // Unique token for link (UUID or random string)
    private String password;    // Store hashed password (e.g. BCrypt)
    private Instant expiresAt;      // Expiration timestamp
    private Instant createdAt = Instant.now();
}
