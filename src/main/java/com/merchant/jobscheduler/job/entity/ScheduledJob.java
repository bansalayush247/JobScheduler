package com.merchant.jobscheduler.job.entity;

import com.merchant.jobscheduler.job.enums.JobStatus;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "scheduled_jobs")
public class ScheduledJob {

    @Id
    @GeneratedValue
    private UUID id;

    private String jobName;

    private String webhookUrl;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private String cronExpression;

    private LocalDateTime nextExecutionTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    private int retryCount;

    private int maxRetries;

    private LocalDateTime lastExecutionTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}