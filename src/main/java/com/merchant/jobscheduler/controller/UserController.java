package com.merchant.jobscheduler.controller;

import com.merchant.jobscheduler.dto.response.UserProfileResponse;
import com.merchant.jobscheduler.service.UserService;
import com.merchant.jobscheduler.entity.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

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

        return userService.getUserProfile(user);
    }
}