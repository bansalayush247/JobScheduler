package com.merchant.jobscheduler.job.controller;

import com.merchant.jobscheduler.job.entity.ScheduledJob;
import com.merchant.jobscheduler.job.enums.JobStatus;
import com.merchant.jobscheduler.job.repository.ScheduledJobRepository;
import com.merchant.jobscheduler.job.dto.CreateJobRequest;
import com.merchant.jobscheduler.job.constants.JobConstants;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.scheduling.support.CronExpression;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final ScheduledJobRepository repository;

    public JobController(ScheduledJobRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<?> createJob(@RequestBody CreateJobRequest request) {

        ScheduledJob job = new ScheduledJob();

        job.setJobName(request.jobName());
        job.setWebhookUrl(request.webhookUrl());
        job.setPayload(request.payload());
        job.setCronExpression(request.cronExpression());
        job.setMaxRetries(
                request.maxRetries() != null
                        ? request.maxRetries()
                        : JobConstants.DEFAULT_MAX_RETRIES
        );

        CronExpression cron =
                CronExpression.parse(request.cronExpression());

        job.setNextExecutionTime(
                cron.next(LocalDateTime.now())
        );

        job.setStatus(JobStatus.PENDING);
        job.setCreatedAt(LocalDateTime.now());

        repository.save(job);

        return ResponseEntity.ok("Job created successfully");
    }
}