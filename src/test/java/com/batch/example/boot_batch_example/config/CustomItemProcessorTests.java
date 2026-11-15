package com.batch.example.boot_batch_example.config;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import com.batch.example.boot_batch_example.model.Person;

public class CustomItemProcessorTests {

	private final CustomItemProcessor processor = new CustomItemProcessor();

	@Test
	void returnsPersonWithoutChangingIt() throws Exception {
		Person person = new Person("1", "Aarav", "Sharma", "9078563421", "aarav@gmail.com");

		Person processedPerson = processor.process(person);

		assertSame(person, processedPerson);
	}
}
