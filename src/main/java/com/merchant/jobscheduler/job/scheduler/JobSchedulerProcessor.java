package com.merchant.jobscheduler.job.scheduler;

import com.merchant.jobscheduler.job.enums.JobStatus;
import com.merchant.jobscheduler.job.entity.ScheduledJob;
import com.merchant.jobscheduler.job.repository.ScheduledJobRepository;
import com.merchant.jobscheduler.job.service.JobService;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;

@Component
@EnableScheduling
public class JobSchedulerProcessor {

    private static final Logger log =
            LoggerFactory.getLogger(JobSchedulerProcessor.class);

    private final ScheduledJobRepository repository;
    private final JobService service;

    public JobSchedulerProcessor(ScheduledJobRepository repository,
                                 JobService service) {
        this.repository = repository;
        this.service = service;
    }

    @Scheduled(fixedDelay = 10000)
    @SchedulerLock(
            name = "jobSchedulerLock",
            lockAtMostFor = "30s"
    )
    public void pollAndExecuteJobs() {

        log.info("Checking for pending jobs...");
        System.out.println("Scheduled Triggered");
        System.out.println("Java time: "+LocalDateTime.now());
        List<ScheduledJob> jobs =
                repository.findByNextExecutionTimeBeforeAndStatusIn(
                        LocalDateTime.now(),
                        List.of(JobStatus.PENDING,
                                JobStatus.RETRY_SCHEDULED)
                );
        System.out.println("Jobs found: "+jobs.size());
        for (ScheduledJob job : jobs) {
            System.out.println("Executing jobs: "+job.getId());
            service.processJob(job);
        }
    }
}