package com.josephken.roors.admin.controller;

import com.josephken.roors.admin.dto.CreateStaffRequest;
import com.josephken.roors.admin.dto.AdminUserResponse;
import com.josephken.roors.admin.dto.UserSummaryResponse;
import com.josephken.roors.auth.dto.MessageResponse;
import com.josephken.roors.auth.entity.User;
import com.josephken.roors.auth.entity.UserRole;
import com.josephken.roors.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class UserAdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<List<UserSummaryResponse>> getAllUsers() {
        List<User> users = userRepository.findAll();

        List<UserSummaryResponse> response = users.stream()
                .map(user -> UserSummaryResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .verified(user.isVerified())
                        .disabled(user.isDisabled())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/disable")
    public ResponseEntity<MessageResponse> disableUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        user.setDisabled(true);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User disabled successfully"));
    }

    @PostMapping("/{id}/enable")
    public ResponseEntity<MessageResponse> enableUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        user.setDisabled(false);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User enabled successfully"));
    }

    @PostMapping("/{id}/verify-staff")
    public ResponseEntity<MessageResponse> verifyStaff(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        user.setRole(UserRole.STAFF);
        user.setVerified(true);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User promoted to STAFF and verified"));
    }

    @PostMapping("/create-staff")
    public ResponseEntity<MessageResponse> createStaff(@Valid @RequestBody CreateStaffRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.STAFF);
        user.setVerified(true);

        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("Staff account created"));
    }
}
