package com.backend.SecureSharing.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadRequestDTO {
    private MultipartFile file;
    private String sensitivity;
    private boolean encrypted;
    private String userId;
}
