package com.mikejuliet.authservice.repositories;

import com.mikejuliet.authservice.entities.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCredentialsRepository extends JpaRepository<UserCredentials,String> {

    Optional<UserCredentials> findUserByUsername(String username);

}
