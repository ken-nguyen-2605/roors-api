package com.josephken.roors.common.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WelcomeController {
    
    @GetMapping("/")
    public String welcome() {
        return "Welcome to Roors API! Try /api/auth/register or /api/auth/login";
    }
    
    @GetMapping("/health")
    public String health() {
        return "API is running";
    }
}