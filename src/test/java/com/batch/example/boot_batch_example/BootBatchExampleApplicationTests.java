package com.batch.example.boot_batch_example;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

@SpringBootTest
@AutoConfigureMockMvc
public class BootBatchExampleApplicationTests {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void mainDelegatesToSpringApplication() {
		try (MockedStatic<SpringApplication> springApplication = org.mockito.Mockito.mockStatic(SpringApplication.class)) {
			BootBatchExampleApplication.main(new String[] {"--test"});

			springApplication.verify(() -> SpringApplication.run(
					BootBatchExampleApplication.class, new String[] {"--test"}));
		}
	}

	@Test
	void configureRegistersTheApplicationSource() {
		SpringApplicationBuilder builder = new SpringApplicationBuilder();

		assertThat(new BootBatchExampleApplication().configure(builder)).isSameAs(builder);
	}

	@Test
	void importEndpointRunsJobAndStoresCsvRows() throws Exception {
		mockMvc.perform(post("/api/import"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("COMPLETED"));

		Integer importedCount = jdbcTemplate.queryForObject(
				"select count(*) from person",
				Integer.class);
		assertThat(importedCount).isEqualTo(5);

		var person = jdbcTemplate.queryForMap(
				"select firstName, lastName, contact, email from person where personId = ?",
				"1");

		assertThat(person)
				.containsEntry("FIRSTNAME", "aarav")
				.containsEntry("LASTNAME", "sharma")
				.containsEntry("CONTACT", "9078563421")
				.containsEntry("EMAIL", "aarav@gmail.com");
	}

}
