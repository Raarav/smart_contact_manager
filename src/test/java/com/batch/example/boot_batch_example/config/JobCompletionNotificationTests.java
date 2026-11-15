package com.batch.example.boot_batch_example.config;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;

public class JobCompletionNotificationTests {

    private final JobCompletionNotificationImpl listener = new JobCompletionNotificationImpl();

    @Test
    void logsJobStart() {
        listener.beforeJob(new JobExecution(1L));
    }

    @Test
    void handlesCompletedAndUnsuccessfulJobs() {
        JobExecution completed = new JobExecution(1L);
        completed.setStatus(BatchStatus.COMPLETED);
        listener.afterJob(completed);

        JobExecution failed = new JobExecution(2L);
        failed.setStatus(BatchStatus.FAILED);
        listener.afterJob(failed);
    }
}
