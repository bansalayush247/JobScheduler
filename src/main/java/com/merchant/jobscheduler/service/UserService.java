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

        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCodes.USER_NOT_FOUND));

        Role newRole = roleRepository.findByName(roleName.toUpperCase()).orElseThrow(() -> new CustomException(ErrorCodes.ROLE_NOT_FOUND));

        String currentRole = user.getRole().getName();

        // 🚫 ADMIN role cannot be changed
        if (ROLE_ADMIN.equalsIgnoreCase(currentRole)) {
            throw new CustomException(ErrorCodes.ADMIN_ROLE_CHANGE_NOT_ALLOWED);
        }

        // 🚨 Enforce single ADMIN rule
        if (ROLE_ADMIN.equalsIgnoreCase(roleName)
                && userRepository.existsByRole_Name(ROLE_ADMIN)) {

            throw new CustomException(ErrorCodes.ADMIN_ALREADY_EXISTS);
        }

        user.setRole(newRole);
        userRepository.save(user);
    }
}