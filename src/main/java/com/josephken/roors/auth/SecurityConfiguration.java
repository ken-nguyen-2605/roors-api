package com.josephken.roors.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {
    
    @Autowired
    private CorsConfigurationSource corsConfigurationSource;
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtTokenFilter authenticationJwtTokenFilter() {
        return new JwtTokenFilter();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public auth endpoints
                        .requestMatchers("/api/auth/register", "/api/auth/login",
                                        "/api/auth/forgot-password", "/api/auth/reset-password",
                                        "/api/auth/verify-email", "/api/auth/resend-verification", "/api/auth/logout").permitAll()
                        // Protected auth endpoints
                        .requestMatchers("/api/auth/**").authenticated()
                        
                        // Public endpoints
                        .requestMatchers("/", "/welcome", "/health").permitAll()
                        .requestMatchers("/api/categories/**", "/api/menu/**").permitAll()
                        .requestMatchers("/api/payments/methods").permitAll()
                        .requestMatchers("/api/tables/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        
                        // Specific reservation endpoints (public)
                        .requestMatchers("/api/reservations/availability", "/api/reservations/date-time-availability").permitAll()
                        
                        // Admin endpoints
                        .requestMatchers("/admin/**").permitAll()
                        .requestMatchers("/api/admin/**").permitAll()  // Temporarily allow public access for testing
                        
                        // Protected endpoints (require authentication)
                        .requestMatchers("/api/users/**", "/api/orders/**", "/api/payments/**", "/api/reservations/**").authenticated()
                        
                        .anyRequest().authenticated());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}