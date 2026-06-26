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
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

        log.info("Create job request received jobName={}", request.jobName());

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

        log.info("Job created successfully jobId={} jobName={}", job.getId(), job.getJobName());
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

        log.info("Fetching all scheduled jobs");

        return repository.findAll();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJob(@PathVariable UUID id) {

        log.info("Delete job request received jobId={}", id);

        if (!repository.existsById(id)) {
            log.warn("Job not found jobId={}", id);

            return ResponseEntity.status(404).body(  new ApiResponse<>(
                    false,
                    "Job not found",
                    id.toString()
            ));
        }

        repository.deleteById(id);

        log.info("Job deleted successfully jobId={}", id);

        return ResponseEntity.ok(  new ApiResponse<>(
                true,
                "Job deleted successfully",
                id.toString()
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobResponse> updateJob(@PathVariable UUID id, @RequestBody UpdateJobRequest request) {

        log.info("Update job request received jobId={}", id);

        JobResponse response = jobService.updateJob(id, request);

        log.info("Job updated successfully jobId={}", id);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<ApiResponse<String>> pauseJob(@PathVariable UUID id) {

        log.info("Pause job request received jobId={}", id);

        jobService.pauseJob(id);

        log.info("Job paused successfully jobId={}", id);

        return ResponseEntity.ok(new ApiResponse<>(true, "Job paused successfully", id.toString()));
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<ApiResponse<String>> resumeJob(@PathVariable UUID id) {

        log.info("Resume job request received jobId={}", id);

        jobService.resumeJob(id);

        log.info("Job resumed successfully jobId={}", id);


        return ResponseEntity.ok(new ApiResponse<>(true, "Job resumed successfully", id.toString()));
    }
}