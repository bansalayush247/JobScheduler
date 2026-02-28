package com.merchant.jobscheduler.dto.response;

public record UserProfileResponse(
        String userId,
        String username,
        String email,
        String role
) {}