package com.backend.SecureSharing.repository;

import com.backend.SecureSharing.entity.ShareLink;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;


public interface ShareRepository extends MongoRepository<ShareLink , String> {
    Optional<ShareLink> findByToken(String token);
}
