package com.merchant.jobscheduler.service;

import com.merchant.jobscheduler.dto.response.UserProfileResponse;
import com.merchant.jobscheduler.entity.Role;
import com.merchant.jobscheduler.entity.User;
import com.merchant.jobscheduler.exception.CustomException;
import com.merchant.jobscheduler.exception.ErrorCodes;
import com.merchant.jobscheduler.repository.RoleRepository;
import com.merchant.jobscheduler.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    // Build profile from authenticated user (passed from controller)
    public UserProfileResponse getUserProfile(User user) {

        return new UserProfileResponse(user.getId().toString(), user.getUsername(), user.getEmail(), user.getRole().getName());
    }

    // Admin upgrades user role
    public void upgradeUserRole(UUID userId, String roleName) {

        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(
                        ErrorCodes.USER_NOT_FOUND,
                        "User not found"
                ));

        Role newRole = roleRepository.findByName(roleName).orElseThrow(() -> new CustomException(
                        ErrorCodes.ROLE_NOT_FOUND,
                        "Role not found"
                ));

        String currentRole = user.getRole().getName();

        if ("ADMIN".equalsIgnoreCase(currentRole)) {
            throw new CustomException(
                    ErrorCodes.ADMIN_ROLE_CHANGE_NOT_ALLOWED,
                    "ADMIN role cannot be changed"
            );
        }
        // 🚨 Enforce single ADMIN rule
        if (roleName.equals("ADMIN") && userRepository.existsByRole_Name("ADMIN")) {

            throw new CustomException(
                    ErrorCodes.ADMIN_ALREADY_EXISTS,
                    "Only one ADMIN allowed in system"
            );
        }

        user.setRole(newRole);
        userRepository.save(user);
    }
}