package com.merchant.jobscheduler.job.dto;

public record CreateJobRequest(
        String jobName,
        String webhookUrl,
        String payload,
        String cronExpression,
        Integer maxRetries
) {}