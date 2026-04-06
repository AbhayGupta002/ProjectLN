package com.example.personalassistant.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.support.BeanDefinitionDsl;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hotellogin")
public class HotelLogin {

    @Id
    @Column(name = "email", nullable = false, updatable = true)
    @NotNull
    private String email;

    @Column(name = "password")
    private String Password;

    @Enumerated(EnumType.STRING)
    private BeanDefinitionDsl.Role role;
}
