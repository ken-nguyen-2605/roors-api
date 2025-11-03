package com.josephken.roors.auth.repository;

import com.josephken.roors.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByResetToken(String resetToken);
    Optional<User> findByVerifyToken(String verifyToken);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
