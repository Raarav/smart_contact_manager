package com.batch.example.boot_batch_example.api;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/import")
public class ImportController {

    private final JobLauncher jobLauncher;
    private final Job importJob;

    public ImportController(JobLauncher jobLauncher, Job jobBean) {
        this.jobLauncher = jobLauncher;
        this.importJob = jobBean;
    }

    @PostMapping
    public ImportJobResponse importContacts() throws JobExecutionException {
        var parameters = new JobParametersBuilder()
                .addLong("requestedAt", System.nanoTime())
                .toJobParameters();
        JobExecution execution = jobLauncher.run(importJob, parameters);

        if (execution.getStatus().isUnsuccessful()) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Contact import job failed: " + execution.getExitStatus().getExitDescription());
        }

        return new ImportJobResponse(execution.getId(), execution.getStatus().toString());
    }

    public record ImportJobResponse(Long executionId, String status) {
    }
}
