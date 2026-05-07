package com.merchant.jobscheduler.controller;

import com.merchant.jobscheduler.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<String> upgradeRole(@PathVariable UUID userId,
                                              @RequestParam String roleName) {

        log.info("Role upgrade request received for userId={} role={}",
                userId,
                roleName);

        userService.upgradeUserRole(userId, roleName);

        log.info("Role updated successfully for userId={}", userId);

        return ResponseEntity.ok("Role updated successfully");
    }
}