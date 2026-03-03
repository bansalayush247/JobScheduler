package com.merchant.jobscheduler.job.service;

import com.merchant.jobscheduler.job.entity.ScheduledJob;
import com.merchant.jobscheduler.job.enums.JobStatus;
import com.merchant.jobscheduler.job.constants.JobConstants;
import com.merchant.jobscheduler.job.executor.JobExecutor;
import com.merchant.jobscheduler.job.repository.ScheduledJobRepository;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.support.CronExpression;

import java.time.LocalDateTime;

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
}