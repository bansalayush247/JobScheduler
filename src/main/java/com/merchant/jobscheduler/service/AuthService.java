package com.merchant.jobscheduler.service;

import com.merchant.jobscheduler.dto.request.LoginRequest;
import com.merchant.jobscheduler.dto.request.RegisterRequest;
import com.merchant.jobscheduler.dto.response.LoginResponse;
import com.merchant.jobscheduler.entity.Role;
import com.merchant.jobscheduler.entity.User;
import com.merchant.jobscheduler.repository.RoleRepository;
import com.merchant.jobscheduler.repository.UserRepository;
import com.merchant.jobscheduler.security.JwtTokenProvider;
import com.merchant.jobscheduler.exception.CustomException;
import com.merchant.jobscheduler.exception.ErrorCodes;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private static final String ROLE_USER = "USER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider jwtProvider;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder encoder,
                       JwtTokenProvider jwtProvider) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.jwtProvider = jwtProvider;
    }

    @Transactional
    public void register(RegisterRequest request) {

        String email = request.email().toLowerCase().trim();

        logger.info("Register attempt for email: {}", email);

        if (userRepository.existsByEmail(email)) {

            logger.error("Registration failed. Email already exists: {}", email);

            throw new CustomException(ErrorCodes.EMAIL_ALREADY_EXISTS);
        }

        Role role = roleRepository.findByName("ROLE_USER").orElseThrow(() -> new CustomException(ErrorCodes.ROLE_NOT_FOUND));

        User user = new User();
        user.setUsername(request.name());
        user.setEmail(email);
        user.setPassword(encoder.encode(request.password()));
        user.setRole(role);

        userRepository.save(user);

        logger.info("User registered successfully: {}",email);
    }

    public LoginResponse login(LoginRequest request) {

        String email = request.email().toLowerCase().trim();

        logger.info("Login attempt for email: {}", email);

        User user = userRepository.findByEmail(email).orElseThrow(() -> {

                    logger.error("Login failed. Invalid credentials for email: {}", email);

                    return new CustomException(ErrorCodes.INVALID_CREDENTIALS);
                });

        if (!encoder.matches(request.password(), user.getPassword())) {

            logger.error("Login failed. Invalid credentials for email: {}", email);

            throw new CustomException(ErrorCodes.INVALID_CREDENTIALS);
        }

        String token = jwtProvider.generateToken(
                user.getId().toString(),
                user.getRole().getName()
        );

        logger.info("Login successful for email: {}", email);

        return new LoginResponse(user.getId().toString(), token);
    }
}