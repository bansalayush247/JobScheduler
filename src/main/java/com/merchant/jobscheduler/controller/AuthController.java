package com.merchant.jobscheduler.controller;

import com.merchant.jobscheduler.dto.request.LoginRequest;
import com.merchant.jobscheduler.dto.request.RegisterRequest;
import com.merchant.jobscheduler.dto.response.LoginResponse;
import com.merchant.jobscheduler.service.AuthService;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger =
            LoggerFactory.getLogger(AuthController.class);

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequest request) {

        logger.info("Register API called for email: {}", request.email());

        service.register(request);

        return ResponseEntity.ok("User Registered Successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        logger.info("Login API called for email: {}", request.email());

        return ResponseEntity.ok(service.login(request));
    }
}