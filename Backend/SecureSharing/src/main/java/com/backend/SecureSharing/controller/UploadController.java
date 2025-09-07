package com.backend.SecureSharing.controller;

import com.backend.SecureSharing.CustomUserDetails;
import com.backend.SecureSharing.DTO.AllUploadsDTO;
import com.backend.SecureSharing.DTO.UploadRequestDTO;
import com.backend.SecureSharing.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@CrossOrigin("http://localhost:1234")
public class UploadController {

    @Autowired
    UploadService uploadService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadData(@RequestParam MultipartFile file , @RequestParam String sensitivity , @RequestParam boolean encrypted) throws Exception {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String userId = userDetails.getID();

        UploadRequestDTO dto = new UploadRequestDTO();
        dto.setFile(file);
        dto.setSensitivity(sensitivity);
        dto.setEncrypted(encrypted);
        dto.setUserId(userId);

        uploadService.uploadData(dto);
        return ResponseEntity.ok("File uploaded successfully!");
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<AllUploadsDTO>> getAllUploads(Authentication authentication){
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String userId = userDetails.getID();
        List<AllUploadsDTO> uploadedFiles = uploadService.getAllUploads(userId);
        return ResponseEntity.ok(uploadedFiles);
    }

}
