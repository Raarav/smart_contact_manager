package com.batch.example.boot_batch_example.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.server.ResponseStatusException;

public class ImportControllerTests {

    @Test
    void returnsExecutionIdAndStatusWhenImportSucceeds() throws Exception {
        JobLauncher jobLauncher = mock(JobLauncher.class);
        Job job = mock(Job.class);
        JobExecution execution = new JobExecution(42L);
        execution.setStatus(BatchStatus.COMPLETED);
        ImportController controller = new ImportController(jobLauncher, job);
        when(jobLauncher.run(eq(job), any(JobParameters.class))).thenReturn(execution);

        ImportController.ImportJobResponse response = controller.importContacts();

        assertThat(response.executionId()).isEqualTo(42L);
        assertThat(response.status()).isEqualTo("COMPLETED");
        verify(jobLauncher).run(eq(job), any(JobParameters.class));
    }

    @Test
    void reportsServerErrorWhenImportJobFails() throws Exception {
        JobLauncher jobLauncher = mock(JobLauncher.class);
        Job job = mock(Job.class);
        JobExecution execution = new JobExecution(42L);
        execution.setStatus(BatchStatus.FAILED);
        execution.setExitStatus(new ExitStatus("FAILED", "Import failed"));
        ImportController controller = new ImportController(jobLauncher, job);
        when(jobLauncher.run(eq(job), any(JobParameters.class))).thenReturn(execution);

        assertThatThrownBy(controller::importContacts)
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Contact import job failed: Import failed");
    }
}
