package com.backend.SecureSharing.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
@Document("uploads")
public class Upload {
    @Id
    private String id;

    private String userId;
    private String fileName;
    private String contentType;
    private long size;


    private String gridFsFileId;


    private byte[] encryptedAesKey;
    private byte[] iv;               // GCM IV used for file encryption
    private String cipher;           // e.g. "AES/GCM/NoPadding"
    private String keyWrap;          // "RSA" or "MASTER"


    private boolean encrypted;       // always true here
    private String sensitivity;      // PUBLIC / PRIVATE / CONFIDENTIAL
}
