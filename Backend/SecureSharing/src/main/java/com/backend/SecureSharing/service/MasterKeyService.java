package com.backend.SecureSharing.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
public class MasterKeyService {

    private final SecretKey kek;

    // Inject from application.properties (master.key.b64=...)
    public MasterKeyService(@Value("${master.key.b64}") String masterKeyB64) {
        byte[] raw = Base64.getDecoder().decode(masterKeyB64);
        if (raw.length != 32) {
            throw new IllegalStateException("Master key must be exactly 32 bytes (256-bit AES)");
        }
        this.kek = new SecretKeySpec(raw, "AES");
    }

    // Wrap a per-file AES key (encrypt it with the master key)
    public byte[] wrap(byte[] keyBytes) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding"); // Simplest, but not RFC3394
        cipher.init(Cipher.ENCRYPT_MODE, kek);
        return cipher.doFinal(keyBytes);
    }

    // Unwrap the stored AES key
    public byte[] unwrap(byte[] wrappedKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, kek);
        return cipher.doFinal(wrappedKey);
    }
}
