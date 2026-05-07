package com.merchant.jobscheduler.service;

import com.merchant.jobscheduler.dto.response.UserProfileResponse;
import com.merchant.jobscheduler.entity.Role;
import com.merchant.jobscheduler.entity.User;
import com.merchant.jobscheduler.exception.CustomException;
import com.merchant.jobscheduler.exception.ErrorCodes;
import com.merchant.jobscheduler.repository.RoleRepository;
import com.merchant.jobscheduler.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {

    private static final String ROLE_ADMIN = "ADMIN";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    // Build profile from authenticated user
    public UserProfileResponse getUserProfile(User user) {
        return new UserProfileResponse(
                user.getId().toString(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().getName()
        );
    }

    // Admin upgrades user role
    @Transactional
    public void upgradeUserRole(UUID userId, String roleName) {

        log.info("Starting role upgrade for userId={} requestedRole={}",
                userId,
                roleName);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found userId={}", userId);
                    return new CustomException(ErrorCodes.USER_NOT_FOUND);
                });

        Role newRole = roleRepository.findByName(roleName.toUpperCase())
                .orElseThrow(() -> {
                    log.error("Role not found roleName={}", roleName);
                    return new CustomException(ErrorCodes.ROLE_NOT_FOUND);
                });

        String currentRole = user.getRole().getName();

        log.info("Current role for userId={} is {}",
                userId,
                currentRole);

        // ADMIN cannot be changed
        if (ROLE_ADMIN.equalsIgnoreCase(currentRole)) {

            log.warn("Attempt to change ADMIN role userId={}", userId);

            throw new CustomException(
                    ErrorCodes.ADMIN_ROLE_CHANGE_NOT_ALLOWED
            );
        }

        // Single admin validation
        if (ROLE_ADMIN.equalsIgnoreCase(roleName)
                && userRepository.existsByRole_Name(ROLE_ADMIN)) {

            log.warn("ADMIN already exists. Cannot assign another ADMIN");

            throw new CustomException(
                    ErrorCodes.ADMIN_ALREADY_EXISTS
            );
        }

        user.setRole(newRole);

        userRepository.save(user);

        log.info("Role upgraded successfully userId={} newRole={}",
                userId,
                roleName);
    }
}