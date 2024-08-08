package com.mikejuliet.authservice.services;

import com.mikejuliet.authservice.entities.UserCredentials;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoder;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class jwtService {

    private AuthorizationCodeGrant codeGrant;
    String secret = "9b22085d051f7a039e65a8ece9f15f02610e25dc52744c37d068606f13bb77a3";

    public String generateToken(UserCredentials credentials,String[] decodedAuthorizationCode){

        if(decodedAuthorizationCode!=null && decodedAuthorizationCode.length >= 7){
            String username = decodedAuthorizationCode[0];
            String sub = decodedAuthorizationCode[1];
            String name = decodedAuthorizationCode[2];
            String issuedAt = decodedAuthorizationCode[3];
            String expiredAt = decodedAuthorizationCode[4];
            String resourceId = decodedAuthorizationCode[5];
            String resourceServerName = decodedAuthorizationCode[6];

            if (!isCodeValid(issuedAt, expiredAt)) {
                throw new RuntimeException("Authorization code is expired or invalid");
            }
            Map<String,Object> Claims = new HashMap<>() ;
            Claims.put("username",credentials.getUsername());
            Claims.put("roles",credentials.getRoles());
            Claims.put("name",name);
            Claims.put("resourceId",resourceId);
            Claims.put("resourceName",resourceServerName);
            return createToken(Claims,credentials.getSubject());

        }
        return null;
    }
    public HttpStatus validateToken(final String token) {
        try{
            Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
            return HttpStatus.OK;
        }
        catch (JwtException e){
            return HttpStatus.UNAUTHORIZED;
        }


    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+2 * 60 * 60 * 1000))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignKey() {
        byte[] key = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(key);
    }
    public boolean isCodeValid(String issuedAt, String expiredAt) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
            Date issuedAtDate = dateFormat.parse(issuedAt);
            Date expiredAtDate = dateFormat.parse(expiredAt);
            Date currentTime = new Date();

            // Check if the current time is within the valid range (issuedAt <= currentTime <= expiredAt)
            return issuedAtDate.compareTo(currentTime) <= 0 && currentTime.compareTo(expiredAtDate) <= 0;
        } catch (ParseException e) {
            // Handle parsing errors, e.g., if the timestamp format is invalid
            e.printStackTrace();
            return false; // Return false if there's an error parsing the timestamps
        }
    }
}
