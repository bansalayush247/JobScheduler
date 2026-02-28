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

@Service
public class AuthService {

    private static final Logger logger =
            LoggerFactory.getLogger(AuthService.class);

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

    public void register(RegisterRequest request) {

        logger.info("Register attempt for email: {}", request.email());

        if (userRepository.existsByEmail(request.email())) {

            logger.error("Registration failed. Email already exists: {}", request.email());

            throw new CustomException(
                    ErrorCodes.EMAIL_ALREADY_EXISTS,
                    "Email already registered"
            );
        }

        Role role = roleRepository.findByName("USER")
                .orElseThrow(() -> new CustomException(
                        ErrorCodes.USER_NOT_FOUND,
                        "Default role not found"
                ));

        User user = new User();
        user.setUsername(request.name());
        user.setEmail(request.email());
        user.setPassword(encoder.encode(request.password()));
        user.setRole(role);

        userRepository.save(user);

        logger.info("User registered successfully: {}", request.email());
    }

    public LoginResponse login(LoginRequest request) {

        logger.info("Login attempt for email: {}", request.email());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {

                    logger.error("Login failed. User not found: {}", request.email());

                    return new CustomException(
                            ErrorCodes.INVALID_CREDENTIALS,
                            "Invalid credentials"
                    );
                });

        if (!encoder.matches(request.password(), user.getPassword())) {

            logger.error("Login failed. Invalid password for email: {}", request.email());

            throw new CustomException(
                    ErrorCodes.INVALID_CREDENTIALS,
                    "Invalid credentials"
            );
        }

        String token = jwtProvider.generateToken(
                user.getId().toString(),
                user.getRole().getName()
        );

        logger.info("Login successful for email: {}", request.email());

        return new LoginResponse(
                user.getId().toString(),
                token
        );
    }
}