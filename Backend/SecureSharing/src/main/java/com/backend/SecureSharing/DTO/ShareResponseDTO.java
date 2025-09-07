package com.backend.SecureSharing.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShareResponseDTO {
    private String token;
    private String shareUrl;
    private long expiresInSeconds;
}
