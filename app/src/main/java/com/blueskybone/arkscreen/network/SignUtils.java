package com.blueskybone.arkscreen.network;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class SignUtils {
    public static byte[] calculateHmacSha256(String message, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmac.init(secretKey);
        return hmac.doFinal(message.getBytes(StandardCharsets.UTF_8));
    }

    public static String calculateMD5(String message) throws NoSuchAlgorithmException {
        byte[] md5 = null;
        md5 = MessageDigest.getInstance("md5").digest(message.getBytes(StandardCharsets.UTF_8));
        BigInteger no = new BigInteger(1, md5);
        StringBuilder hashText = new StringBuilder(no.toString(16));
        while (hashText.length() < 32) {
            hashText.insert(0, "0");
        }
        return hashText.toString();
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public static String generateSign(String api, String params, String key, String timeStamp) throws NoSuchAlgorithmException, InvalidKeyException {


        String jsonArgs = "{\"platform\":\"\",\"timestamp\":\"" + timeStamp + "\",\"dId\":\"\",\"vName\":\"\"}";
        String data = api + params + timeStamp + jsonArgs;

        byte[] hmacData = calculateHmacSha256(data, key);
        String hmacSha256Hex = bytesToHex(hmacData);

        return calculateMD5(hmacSha256Hex);
    }
}
