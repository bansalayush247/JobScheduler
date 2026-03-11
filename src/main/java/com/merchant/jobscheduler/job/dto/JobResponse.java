package com.merchant.jobscheduler.job.dto;

import com.merchant.jobscheduler.job.enums.JobStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record JobResponse(


        UUID jobId,
        String jobName,
        JobStatus status,
        String cronExpression,
        LocalDateTime nextExecutionTime

) {}