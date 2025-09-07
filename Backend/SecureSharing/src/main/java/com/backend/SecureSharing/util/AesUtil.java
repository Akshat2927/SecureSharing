package com.backend.SecureSharing.util;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;

public class AesUtil {
    public static final String AES_CIPHER = "AES/GCM/NoPadding";
    private static final int AES_KEY_BITS = 256;      // ensure JCE unlimited or Java 9+
    private static final int GCM_TAG_BITS = 128;
    private static final int GCM_IV_BYTES = 12;

    public static SecretKey generateKey() throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(AES_KEY_BITS);
        return kg.generateKey();
    }

    public static byte[] randomIV() {
        byte[] iv = new byte[GCM_IV_BYTES];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    public static void encryptStream(InputStream in, OutputStream out, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_CIPHER);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_BITS, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);
        try (CipherOutputStream cos = new CipherOutputStream(out, cipher)) {
            in.transferTo(cos);
        }
    }

    public static void decryptStream(InputStream in, OutputStream out, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_CIPHER);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_BITS, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        try (CipherInputStream cis = new CipherInputStream(in, cipher)) {
            cis.transferTo(out);
        }
    }
}
