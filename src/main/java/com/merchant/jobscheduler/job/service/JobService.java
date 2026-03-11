package com.merchant.jobscheduler.job.service;

import com.merchant.jobscheduler.job.entity.ScheduledJob;
import com.merchant.jobscheduler.job.enums.JobStatus;
import com.merchant.jobscheduler.job.constants.JobConstants;
import com.merchant.jobscheduler.job.executor.JobExecutor;
import com.merchant.jobscheduler.job.repository.ScheduledJobRepository;
import com.merchant.jobscheduler.job.dto.UpdateJobRequest;
import com.merchant.jobscheduler.job.dto.JobResponse;
import com.merchant.jobscheduler.exception.CustomException;
import com.merchant.jobscheduler.exception.ErrorCodes;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.support.CronExpression;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class JobService {

    private final ScheduledJobRepository repository;
    private final JobExecutor executor;

    public JobService(ScheduledJobRepository repository,
                      JobExecutor executor) {
        this.repository = repository;
        this.executor = executor;
    }

    public void processJob(ScheduledJob job) {

        try {
            executor.execute(job);

            job.setStatus(JobStatus.SUCCESS);
            job.setRetryCount(0);
            job.setLastExecutionTime(LocalDateTime.now());

            scheduleNext(job);

        } catch (Exception e) {

            int retry = job.getRetryCount() + 1;
            job.setRetryCount(retry);

            if (retry < job.getMaxRetries()) {

                job.setStatus(JobStatus.RETRY_SCHEDULED);
                job.setNextExecutionTime(
                        LocalDateTime.now().plusSeconds(30 * (retry))
                );

            } else {
                job.setStatus(JobStatus.FAILED);
            }
        }
        repository.save(job);
    }

    private void scheduleNext(ScheduledJob job) {

        CronExpression cron =
                CronExpression.parse(job.getCronExpression());

        LocalDateTime next =
                cron.next(LocalDateTime.now());

        job.setNextExecutionTime(next);
        job.setStatus(JobStatus.PENDING);
    }

    public JobResponse updateJob(UUID id, UpdateJobRequest request) {

        ScheduledJob job = repository.findById(id)
                .orElseThrow(() -> new CustomException(
                        ErrorCodes.JOB_NOT_FOUND,
                        "Job not found"
                ));

        if (request.getJobName() != null) {
            job.setJobName(request.getJobName());
        }

        if (request.getWebhookUrl() != null) {
            job.setWebhookUrl(request.getWebhookUrl());
        }

        if (request.getPayload() != null) {
            job.setPayload(request.getPayload());
        }

        if (request.getCronExpression() != null) {

            job.setCronExpression(request.getCronExpression());

            CronExpression cron = CronExpression.parse(request.getCronExpression());

            job.setNextExecutionTime(
                    cron.next(LocalDateTime.now())
            );
        }

        job.setUpdatedAt(LocalDateTime.now());

        repository.save(job);

        return mapToResponse(job);
    }

    private JobResponse mapToResponse(ScheduledJob job) {

        return new JobResponse(
                job.getId(),
                job.getJobName(),
                job.getStatus(),
                job.getCronExpression(),
                job.getNextExecutionTime()
        );
    }
}