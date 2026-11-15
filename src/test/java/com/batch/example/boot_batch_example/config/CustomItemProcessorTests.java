package com.batch.example.boot_batch_example.config;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import com.batch.example.boot_batch_example.model.Person;

public class CustomItemProcessorTests {

	private final CustomItemProcessor processor = new CustomItemProcessor();

	@Test
	void returnsPersonWithoutChangingIt() throws Exception {
		Person person = new Person("1", "Sample", "One", "+1-202-555-0101", "sample1@example.com");

		Person processedPerson = processor.process(person);

		assertSame(person, processedPerson);
	}
}
