package com.example.personalassistant.entity;

import lombok.Data;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Data
@Entity
@Table(name = "admin_credential")
public class AdminLogin {
    @Id
    @Column(name = "email", nullable = false,updatable = false)
    private String email;

    @Column(name = "password")
    private String password;

}
