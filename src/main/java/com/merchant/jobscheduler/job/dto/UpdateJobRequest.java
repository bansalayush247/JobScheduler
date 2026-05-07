package com.merchant.jobscheduler.job.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateJobRequest {

    private String jobName;
    private String webhookUrl;
    private String payload;
    private String cronExpression;
}