package com.mikejuliet.authservice.config;



import com.mikejuliet.authservice.entities.UserCredentials;
import com.mikejuliet.authservice.repositories.UserCredentialsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserCredentialsRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserCredentials> credential = repository.findUserByUsername(username);
        return credential
                .map(CustomUserDetails::new)
                .orElseThrow(
                        () -> new UsernameNotFoundException("user not found with username :" + username)
                );
    }
}