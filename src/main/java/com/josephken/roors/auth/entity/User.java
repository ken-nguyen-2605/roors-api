package com.josephken.roors.auth.entity;

import com.josephken.roors.menu.entity.MenuItem;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    private String username;
    private String password;
    private String email;
    
    @Column(name = "full_name")
    private String fullName;
    
    @Column(name = "contact_number")
    private String contactNumber;
    
    @Column(name = "profile_image")
    private String profileImage;
    
    @Column(columnDefinition = "TEXT")
    private String address;
    
    @Column(name = "member_since", updatable = false)
    private LocalDateTime memberSince;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "is_verified")
    private boolean isVerified = false;
    
    @Column(name = "verify_token")
    private String verifyToken;
    
    @Column(name = "verify_token_expiry")
    private LocalDateTime verifyTokenExpiry;

    @Column(name = "reset_token")
    private String resetToken;
    
    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_liked_dishes",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "menu_item_id")
    )
    private Set<MenuItem> likedDishes = new HashSet<>();
    
    @PrePersist
    protected void onCreate() {
        if (memberSince == null) {
            memberSince = LocalDateTime.now();
        }
    }

    // UserDetails methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isVerified;
    }

}