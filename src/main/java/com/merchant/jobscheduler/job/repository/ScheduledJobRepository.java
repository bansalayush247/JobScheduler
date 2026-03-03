package com.merchant.jobscheduler.job.repository;

import com.merchant.jobscheduler.job.entity.ScheduledJob;
import com.merchant.jobscheduler.job.enums.JobStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ScheduledJobRepository extends JpaRepository<ScheduledJob, UUID> {

    List<ScheduledJob> findByNextExecutionTimeBeforeAndStatusIn(
            LocalDateTime time,
            List<JobStatus> statuses
    );
}