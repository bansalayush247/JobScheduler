package com.merchant.jobscheduler.controller;

import com.merchant.jobscheduler.dto.response.UserProfileResponse;
import com.merchant.jobscheduler.entity.User;
import com.merchant.jobscheduler.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public UserProfileResponse profile(HttpServletRequest request) {

        User user = (User) request.getAttribute("authenticatedUser");

        log.info("Profile API called for userId={} email={}",
                user.getId(),
                user.getEmail());

        return userService.getUserProfile(user);
    }
}