package com.api.bugzapper.util;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Component
public class EncryptionUtil {
    private static String key = "1234567812345678";
    private static String initVector = "1234567812345678";
    private static String algo = "AES/CBC/PKCS5PADDING";

    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public static String encrypt(String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance(algo);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            return base62Encode(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String encrypted) {
        try {
            byte[] decodedEncrypted = base62Decode(encrypted);

            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance(algo);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(decodedEncrypted);
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static String base62Encode(byte[] input) {
        BigInteger bigInt = new BigInteger(1, input);
        StringBuilder result = new StringBuilder();
        while (bigInt.compareTo(BigInteger.ZERO) > 0) {
            int mod = bigInt.mod(BigInteger.valueOf(BASE62.length())).intValue();
            result.append(BASE62.charAt(mod));
            bigInt = bigInt.divide(BigInteger.valueOf(BASE62.length()));
        }
        return result.reverse().toString();
    }

    private static byte[] base62Decode(String input) {
        BigInteger bigInt = BigInteger.ZERO;
        for (char c : input.toCharArray()) {
            bigInt = bigInt.multiply(BigInteger.valueOf(BASE62.length()));
            bigInt = bigInt.add(BigInteger.valueOf(BASE62.indexOf(c)));
        }
        byte[] bytes = bigInt.toByteArray();
        if (bytes[0] == 0) {
            bytes = Arrays.copyOfRange(bytes, 1, bytes.length);
        }
        return bytes;
    }
}
