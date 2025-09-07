package com.backend.SecureSharing.controller;

import com.backend.SecureSharing.DTO.PasswordDTO;
import com.backend.SecureSharing.DTO.ShareRequestDTO;
import com.backend.SecureSharing.DTO.ShareResponseDTO;
import com.backend.SecureSharing.entity.ShareLink;
import com.backend.SecureSharing.entity.Upload;
import com.backend.SecureSharing.repository.UploadRepository;
import com.backend.SecureSharing.service.ShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ShareController {

    @Autowired
    ShareService shareService;
    @Autowired
    UploadRepository uploadRepository;

    @PostMapping("/create")
    public ResponseEntity<ShareResponseDTO> shareData(@RequestBody ShareRequestDTO shareData , Authentication authentication){
        String userId = authentication.getName(); // from JWT
        return ResponseEntity.ok(shareService.createShareLink(userId, shareData));
    }

    @PostMapping("/access/{token}")
    public ResponseEntity<?> accessFile(
            @PathVariable String token,
            @RequestBody PasswordDTO passDTO
    ) {
        try {
            String password = passDTO.getPassword();
            // ✅ get decrypted file bytes
            ShareLink link = shareService.validateShareLink(token, password);
            byte[] decryptedBytes = shareService.accessSharedFile(token, password);
            Upload upload = uploadRepository.findById(link.getFileId())
                    .orElseThrow(() -> new RuntimeException("File not found"));


            // ✅ only return raw data
            Map<String, Object> response = new HashMap<>();
            response.put("fileName", upload.getFileName());
            response.put("contentType", upload.getContentType());
            response.put("base64Data", Base64.getEncoder().encodeToString(decryptedBytes));

            return ResponseEntity.ok(response);

        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
