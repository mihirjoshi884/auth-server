package com.mikejuliet.authservice.services;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
public class AuthorizationCodeGrant {


    private static final String secretKey = "9b22085d051f7a039e65a8ece9f15f02610e25dc52744c37d068606f13bb77a3";
    private static Date issuedAt;
    private static Date expiredAt;


    private String encodeAuthorizationCode;

    public String generateAuthorizationCode(String username,
                                            String sub,
                                            String name,
                                            String resourceId,
                                            String resourceServerName){

        issuedAt = new Date(System.currentTimeMillis());
        expiredAt = new Date(System.currentTimeMillis() + 10 * 60 * 1000);

        SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy");
        String issuedAtFormatted = sdf.format(issuedAt);
        String expiredAtFormatted = sdf.format(expiredAt);

        String json = "{\"username\":\"" + username + "\",\"name\":\"" + name + "\",\"subId\":\"" + sub + "\",\"issuedAt\":\"" + issuedAtFormatted + "\",\"expiredAt\":\"" + expiredAtFormatted + "\",\"resourceId\":\"" + resourceId + "\",\"resourceServerName\":\"" + resourceServerName + "\"}";

        try{
            // Sign the JSON object using HMAC-SHA256
            Mac sha_256HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8),"HmacSHA256");
            sha_256HMAC.init(secret_key);
            byte[] signatureBytes = sha_256HMAC.doFinal(json.getBytes(StandardCharsets.UTF_8));

            // Encode the signature and JSON object into Base64
            String encodedSignature = Base64.getEncoder().encodeToString(signatureBytes);
            String encodedJson = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));

            // Combine the encoded signature and JSON object
            String authorizationCode = encodedSignature+"|"+encodedJson;
            // append a random UUID for uniqueness
            authorizationCode+= "|"+ UUID.randomUUID().toString();

            return encodeAuthorizationCode(authorizationCode);

        }catch(NoSuchAlgorithmException | InvalidKeyException e){
            e.printStackTrace();
            return null;
        }

    }

    public String[] decodeAuthenticationCode(String authorizationCode){
        String[] components = authorizationCode.split("\\|");
        if (components.length < 2) {
            System.err.println("Invalid authorization code format");
            return null;
        }
        // Extract encoded signature and encoded JSON object
        String encodedSignature = components[0];
        String encodedJson = components[1];

        try{
            // Decode encoded signature and encoded JSON object from Base64
            byte[] signatureBytes = Base64.getDecoder().decode(encodedSignature);
            byte[] jsonBytes = Base64.getDecoder().decode(encodedJson);

            // Verify the signature using HMAC-SHA256
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] calculatedSignature = sha256_HMAC.doFinal(jsonBytes);

            // Compare the calculated signature with the decoded signature
            if (!MessageDigest.isEqual(signatureBytes, calculatedSignature)) {
                System.err.println("Signature verification failed");
                return null;
            }
            // Decode JSON object to retrieve original information
            String json = new String(jsonBytes, StandardCharsets.UTF_8);
            // Parse JSON string into JSON object
            JSONObject jsonObject = new JSONObject(json);

            // Extract individual values from JSON object
            String username = jsonObject.getString("username");
            String subId = jsonObject.getString("subId");
            String name = jsonObject.getString("name");
            String issuedAt = jsonObject.getString("issuedAt");
            String expiredAt = jsonObject.getString("expiredAt");
            String resourceId = jsonObject.getString("resourceId");
            String resourceServerName = jsonObject.getString("resourceServerName");

            // Return the extracted values as an array
            return new String[]{username, subId, name, issuedAt, expiredAt, resourceId, resourceServerName};


        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
    }
    private String encodeAuthorizationCode(String code){
        return URLEncoder.encode(code, StandardCharsets.UTF_8);
    }

}
