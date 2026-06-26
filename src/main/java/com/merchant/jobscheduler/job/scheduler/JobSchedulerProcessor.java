package com.merchant.jobscheduler.job.scheduler;

import com.merchant.jobscheduler.job.enums.JobStatus;
import com.merchant.jobscheduler.job.entity.ScheduledJob;
import com.merchant.jobscheduler.job.repository.ScheduledJobRepository;
import com.merchant.jobscheduler.job.service.JobService;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@EnableScheduling
public class JobSchedulerProcessor {

    private static final Logger log = LoggerFactory.getLogger(JobSchedulerProcessor.class);

    private final ScheduledJobRepository repository;
    private final JobService service;

    public JobSchedulerProcessor(ScheduledJobRepository repository, JobService service) {
        this.repository = repository;
        this.service = service;
    }

    @Scheduled(fixedDelay = 10000)
    @SchedulerLock(name = "jobSchedulerLock", lockAtMostFor = "30s")
    public void pollAndExecuteJobs() {

        String cronId = UUID.randomUUID().toString();

        MDC.put("cronId", cronId);

        try {

            log.info("Checking for pending jobs at {}", LocalDateTime.now());

            List<ScheduledJob> jobs =
                    repository.findByNextExecutionTimeBeforeAndStatusIn(
                            LocalDateTime.now(),
                            List.of(
                                    JobStatus.PENDING,
                                    JobStatus.RETRY_SCHEDULED
                            )
                    );

            log.info("Jobs found: {}", jobs.size());

            for (ScheduledJob job : jobs) {

                MDC.put("jobId", job.getId().toString());

                try {

                    log.info("Executing job");

                    service.processJob(job);

                } catch (Exception ex) {

                    log.error("Job execution failed", ex);

                } finally {

                    MDC.remove("jobId");
                }
            }

        } finally {

            MDC.remove("cronId");
        }
    }
}