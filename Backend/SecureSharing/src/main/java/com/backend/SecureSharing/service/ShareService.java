package com.backend.SecureSharing.service;

import com.backend.SecureSharing.DTO.ShareRequestDTO;
import com.backend.SecureSharing.DTO.ShareResponseDTO;
import com.backend.SecureSharing.entity.ShareLink;
import com.backend.SecureSharing.entity.Upload;
import com.backend.SecureSharing.repository.ShareRepository;
import com.backend.SecureSharing.repository.UploadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class ShareService {

    @Autowired
    ShareRepository shareRepository;
    @Autowired
    BCryptPasswordEncoder passwordEncoder;
    @Autowired
    UploadRepository uploadRepository;
    @Autowired
    UploadService uploadService;

    public ShareResponseDTO createShareLink(String userId, ShareRequestDTO shareData) {
        String token = UUID.randomUUID().toString().replace("-", "");
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(shareData.getExpiresInSeconds());

        ShareLink shareLink = ShareLink.builder()
                .fileId(shareData.getFileId())
                .token(token)
                .password(passwordEncoder.encode(shareData.getPassword()))
                .createdAt(now)
                .expiresAt(expiresAt)
                .build();

        shareRepository.save(shareLink);


        String shareUrl = "http://localhost:1234/share/" + token;


        
        return new ShareResponseDTO(token, shareUrl, shareData.getExpiresInSeconds());
    }

    public ShareLink validateShareLink(String token, String password) {
        ShareLink shareLink = shareRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (Instant.now().isAfter(shareLink.getExpiresAt())) {
            throw new RuntimeException("Link expired");
        }

        if (!passwordEncoder.matches(password, shareLink.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return shareLink;
    }

    public byte[] accessSharedFile(String token, String password) {
        // validate first
        ShareLink link = validateShareLink(token, password);

        // fetch original file
        Upload upload = uploadRepository.findById(link.getFileId())
                .orElseThrow(() -> new RuntimeException("File not found"));

        // decrypt and return file bytes
        return uploadService.decryptData(upload);
    }
}
