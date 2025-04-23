package com.example.userservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table (name="AppUser")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)

    private UUID id;

    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String password;

}