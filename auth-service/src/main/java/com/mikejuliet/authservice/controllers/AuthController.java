package com.mikejuliet.authservice.controllers;


import com.mikejuliet.authservice.entities.AuthRequest;
import com.mikejuliet.authservice.entities.AuthResponse;
import com.mikejuliet.authservice.entities.UserCredentials;
import com.mikejuliet.authservice.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;


@RestController
@RequestMapping("/auth")
public class AuthController {

    //base url : http://localhost:9001/auth/
    @Autowired
    private AuthService service;

    @Autowired
    private AuthResponse authResponse;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register") //url : http://localhost:9001/auth/register
    public String addNewUser(@RequestBody UserCredentials user) {
        return service.saveUser(user);
    }

    @PostMapping("/login") // url:http://localhost:9001/auth/login
    public AuthResponse userAuthentication(@RequestBody AuthRequest authRequest){
        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        if (authenticate.isAuthenticated()) {
            authResponse.setCode(service.generateAuthorizationCode(authRequest.getUsername()));
            authResponse.setMessage("login successfull!!");
            authResponse.setStatus(HttpStatus.OK);
            return authResponse;
        } else {

            throw new RuntimeException("login unsuccessfull!! username and password does not match");
        }
    }

    @PostMapping("/token") //url: http://localhost:9001/auth/token
    public AuthResponse getToken(@RequestBody Map<String, String> requestBody) {
        String code = requestBody.get("code");
        authResponse.setStatus(HttpStatus.OK);
        authResponse.setCode(service.generateToken(code));
        authResponse.setMessage("authorization jwt token is sent!!");
        return authResponse;
    }

    @GetMapping("/validate") //url: http://localhost:9001/auth/validate
    public AuthResponse validateToken(@RequestParam("token") String token) {
        HttpStatus status = service.validateToken(token);
        if(status.equals(HttpStatus.OK)){
            authResponse.setCode(token);
            authResponse.setMessage("Token is valid");
            authResponse.setStatus(status);
            return authResponse;
        }
        else {
            authResponse.setStatus(status);
            authResponse.setMessage("Token is invalid");
            authResponse.setCode(token);
            return authResponse;
        }

    }
}