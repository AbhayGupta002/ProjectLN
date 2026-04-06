package com.example.personalassistant.repository;

import com.example.personalassistant.entity.User;
import com.example.personalassistant.enums.AccountEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    Optional<User> findByMobile(String mobile);
    User findByResetToken(String resetToken);
    List<User> findByAccountEnum(AccountEnum accountEnum);

    @Modifying
    @Query("UPDATE User u SET u.accountEnum='SUSPENDED' WHERE u.id=:id")
    int suspendUser(@Param("id") Long id);

}
