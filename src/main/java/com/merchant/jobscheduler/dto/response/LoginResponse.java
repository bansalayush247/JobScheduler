package com.merchant.jobscheduler.dto.response;

public record LoginResponse(
        String userId,
        String token
) {}