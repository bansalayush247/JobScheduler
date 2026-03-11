package com.merchant.jobscheduler.job.executor;

import com.merchant.jobscheduler.job.entity.ScheduledJob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Map;

@Component
public class JobExecutor {

    private static final Logger log =
            LoggerFactory.getLogger(JobExecutor.class);

    private final RestTemplate restTemplate = new RestTemplate();

    public void execute(ScheduledJob job) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body =
                    Map.of("text", job.getPayload());

            HttpEntity<Map<String, String>> request =
                    new HttpEntity<>(body, headers);

            ResponseEntity<String> response =
                    restTemplate.postForEntity(
                            job.getWebhookUrl(),
                            request,
                            String.class
                    );

            log.info("Webhook response status={}", response.getStatusCode());
            log.debug("Webhook response body={}", response.getBody());
            log.info("Job executed successfully: {}", job.getId());

        } catch (Exception e) {
            log.error("Job execution failed: {}", job.getId(), e);
            throw e;
        }
    }
}