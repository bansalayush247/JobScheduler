package com.merchant.jobscheduler.job.controller;

import com.merchant.jobscheduler.job.entity.ScheduledJob;
import com.merchant.jobscheduler.job.enums.JobStatus;
import com.merchant.jobscheduler.job.repository.ScheduledJobRepository;
import com.merchant.jobscheduler.job.dto.CreateJobRequest;
import com.merchant.jobscheduler.job.dto.UpdateJobRequest;
import com.merchant.jobscheduler.job.constants.JobConstants;
import com.merchant.jobscheduler.job.service.JobService;
import com.merchant.jobscheduler.job.dto.JobResponse;
import com.merchant.jobscheduler.job.dto.ApiResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.scheduling.support.CronExpression;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final ScheduledJobRepository repository;
    private final JobService jobService;

    public JobController(ScheduledJobRepository repository, JobService jobService) {
        this.repository = repository;
        this.jobService = jobService;
    }

    @PostMapping
    public ResponseEntity<JobResponse> createJob(@RequestBody CreateJobRequest request) {

        ScheduledJob job = new ScheduledJob();

        job.setJobName(request.jobName());
        job.setWebhookUrl(request.webhookUrl());
        job.setPayload(request.payload());
        job.setCronExpression(request.cronExpression());

        job.setMaxRetries(request.maxRetries() != null ? request.maxRetries() : JobConstants.DEFAULT_MAX_RETRIES);

        CronExpression cron = CronExpression.parse(request.cronExpression());

        job.setNextExecutionTime(cron.next(LocalDateTime.now()));

        job.setStatus(JobStatus.PENDING);
        job.setCreatedAt(LocalDateTime.now());

        repository.save(job);

        JobResponse response = new JobResponse(
                job.getId(),
                job.getJobName(),
                job.getStatus(),
                job.getCronExpression(),
                job.getNextExecutionTime()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public List<ScheduledJob> getAllJobs() {
        return repository.findAll();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJob(@PathVariable UUID id) {

        if (!repository.existsById(id)) {
            return ResponseEntity.status(404).body("Job not found");
        }

        repository.deleteById(id);

        return ResponseEntity.ok("Job deleted successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobResponse> updateJob(@PathVariable UUID id, @RequestBody UpdateJobRequest request) {

        JobResponse response = jobService.updateJob(id, request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<ApiResponse<String>> pauseJob(@PathVariable UUID id) {

        jobService.pauseJob(id);

        return ResponseEntity.ok(new ApiResponse<>(true, "Job paused successfully", id.toString()));
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<ApiResponse<String>> resumeJob(@PathVariable UUID id) {

        jobService.resumeJob(id);

        return ResponseEntity.ok(new ApiResponse<>(true, "Job resumed successfully", id.toString()));
    }
}