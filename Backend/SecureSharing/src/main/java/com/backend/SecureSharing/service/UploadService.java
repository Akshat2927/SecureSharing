package com.backend.SecureSharing.service;

import com.backend.SecureSharing.DTO.AllUploadsDTO;
import com.backend.SecureSharing.DTO.UploadRequestDTO;
import com.backend.SecureSharing.entity.Upload;
import com.backend.SecureSharing.repository.UploadRepository;
import com.backend.SecureSharing.util.AesUtil;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UploadService {

    private final UploadRepository uploadRepository;
    private final GridFsTemplate gridFsTemplate;
    private final MasterKeyService masterKeyService;

    public void uploadData(UploadRequestDTO requestDTO) throws Exception {
        // 1) Generate per-file AES key + IV
        SecretKey aesKey = AesUtil.generateKey();
        byte[] iv = AesUtil.randomIV();

        // 2) Encrypt file
        ByteArrayOutputStream encBuffer = new ByteArrayOutputStream();
        AesUtil.encryptStream(requestDTO.getFile().getInputStream(), encBuffer, aesKey, iv);
        byte[] encryptedBytes = encBuffer.toByteArray();

        // 3) Wrap AES key always with master key
        byte[] wrappedAesKey = masterKeyService.wrap(aesKey.getEncoded());
        String keyWrap = "MASTER";

        // 4) Store encrypted file in GridFS
        GridFSUploadOptions opts = new GridFSUploadOptions()
                .chunkSizeBytes(1024 * 255)
                .metadata(new org.bson.Document("owner", requestDTO.getUserId())
                        .append("encrypted", true)
                        .append("cipher", AesUtil.AES_CIPHER)
                        .append("iv", iv));

        ObjectId gridId = gridFsTemplate.store(
                new ByteArrayInputStream(encryptedBytes),
                requestDTO.getFile().getOriginalFilename(),
                requestDTO.getFile().getContentType(),
                opts
        );

        // 5) Save metadata in Upload collection
        Upload upload = Upload.builder()
                .userId(requestDTO.getUserId())
                .fileName(requestDTO.getFile().getOriginalFilename())
                .contentType(requestDTO.getFile().getContentType())
                .size(requestDTO.getFile().getSize())
                .gridFsFileId(gridId.toHexString())
                .encrypted(true)
                .cipher(AesUtil.AES_CIPHER)
                .iv(iv)
                .encryptedAesKey(wrappedAesKey)
                .keyWrap(keyWrap)
                .build();

        uploadRepository.save(upload);
    }


    public List<AllUploadsDTO> getAllUploads(String userId) {
        List<Upload> uploads = uploadRepository.findByUserId(userId);

        return uploads.stream()
                .map(upload -> AllUploadsDTO.builder()
                        .id(upload.getId())
                        .fileName(upload.getFileName())
                        .build())
                .collect(Collectors.toList());
    }
    public byte[] decryptData(Upload upload) {
        try {
            // 1) Get encrypted file from GridFS
            GridFSFile gridFile = gridFsTemplate.findOne(
                    Query.query(Criteria.where("_id").is(new ObjectId(upload.getGridFsFileId())))
            );
            if (gridFile == null) {
                throw new RuntimeException("File not found in GridFS");
            }

            try (
                    InputStream encryptedStream = gridFsTemplate.getResource(gridFile).getInputStream();
                    ByteArrayOutputStream decryptedOut = new ByteArrayOutputStream()
            ) {
                // 2) Unwrap per-file AES key using MasterKey
                byte[] rawAesKey = masterKeyService.unwrap(upload.getEncryptedAesKey());
                SecretKey aesKey = new SecretKeySpec(rawAesKey, "AES");

                // 3) Decrypt file using AES key + IV
                AesUtil.decryptStream(encryptedStream, decryptedOut, aesKey, upload.getIv());

                return decryptedOut.toByteArray();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while decrypting file", e);
        }
    }


}
