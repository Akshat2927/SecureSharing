package com.backend.SecureSharing.repository;

import com.backend.SecureSharing.entity.Upload;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UploadRepository extends MongoRepository<Upload,String> {
    List<Upload> findByUserId(String userId);
}
