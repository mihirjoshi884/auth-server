package com.mikejuliet.authservice.services;

import com.mikejuliet.authservice.entities.ResourceServerManager;
import com.mikejuliet.authservice.entities.UserCredentials;
import com.mikejuliet.authservice.repositories.UserCredentialsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserCredentialsRepository repository;
    @Autowired
    private PasswordEncoder encodePassword;
    @Autowired
    private jwtService jwtService;
    @Autowired
    private AuthorizationCodeGrant codeGrant;
    @Autowired
    private ResourceServerManager resourceServerManager;


    public String saveUser(UserCredentials credential) {
        credential.setSubject(UUID.randomUUID().toString());
        credential.setPassword(encodePassword.encode((credential.getPassword())));
        repository.save(credential);
        return "user added to the system";
    }
    private String decodeAuthorizationCode(String encodedCode){
        return URLDecoder.decode(encodedCode, StandardCharsets.UTF_8);
    }
    public String generateToken(String code) {
        String decodedCode = decodeAuthorizationCode(code);
        String[] decodedAuthorizationCode = codeGrant.decodeAuthenticationCode(decodedCode);
        if(decodedAuthorizationCode!=null ){
            String username = decodedAuthorizationCode[0];
            Optional<UserCredentials> user = repository.findUserByUsername(username);
            if(user.isPresent()){
                UserCredentials credentials = user.get();
                return jwtService.generateToken(credentials,decodedAuthorizationCode);
            }else{
                throw new RuntimeException("user not found");
            }
        }

        return null;
    }
    public String generateAuthorizationCode(String username){
        Optional<UserCredentials> user = repository.findUserByUsername(username);
        if(user.isPresent()){
            UserCredentials credentials = user.get();
            return codeGrant.generateAuthorizationCode(username,
                    credentials.getSubject(),
                    credentials.getName(),
                    resourceServerManager.getResourceServerIdByName("user-service"),
                    resourceServerManager.getResourceServerNameByName("user-service"));
        }else{
            throw new RuntimeException("user not found");
        }

    }
    public HttpStatus validateToken(String token) {
        return jwtService.validateToken(token);
    }


}
