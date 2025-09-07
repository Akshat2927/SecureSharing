package com.backend.SecureSharing.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShareRequestDTO {
    private String fileId;
    private String password;
    private long expiresInSeconds;
}
