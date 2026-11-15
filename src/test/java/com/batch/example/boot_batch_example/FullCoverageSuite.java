package com.batch.example.boot_batch_example;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import com.batch.example.boot_batch_example.api.ContactControllerTests;
import com.batch.example.boot_batch_example.api.ImportControllerTests;
import com.batch.example.boot_batch_example.config.CustomItemProcessorTests;
import com.batch.example.boot_batch_example.config.JobCompletionNotificationTests;
import com.batch.example.boot_batch_example.model.PersonTests;

@Suite
@SelectClasses({
        BootBatchExampleApplicationTests.class,
        ContactControllerTests.class,
        ImportControllerTests.class,
        CustomItemProcessorTests.class,
        JobCompletionNotificationTests.class,
        PersonTests.class
})
public class FullCoverageSuite {
}
