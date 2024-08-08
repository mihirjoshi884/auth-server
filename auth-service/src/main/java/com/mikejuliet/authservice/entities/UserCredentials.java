package com.mikejuliet.authservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor
@Entity @Table(name="user_credentials", uniqueConstraints = {@UniqueConstraint(columnNames = "username")})
public class UserCredentials {

    @Id
    private String subject;
    private String name;
    @ElementCollection
    private List<String> roles;
    private String username;
    private String password;
    private String email;
    private String phone;
}
