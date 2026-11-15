package com.batch.example.boot_batch_example.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

public class ApiRecordTests {

    @Test
    void contactRequestExposesItsValuesAndRecordValueSemantics() {
        ContactRequest request = new ContactRequest(
                "Ada", "Lovelace", "555-0100", "ada@example.com", "Analytical Engines", "Mathematician", "Notes");
        ContactRequest equalRequest = new ContactRequest(
                "Ada", "Lovelace", "555-0100", "ada@example.com", "Analytical Engines", "Mathematician", "Notes");

        assertThat(request.firstName()).isEqualTo("Ada");
        assertThat(request.lastName()).isEqualTo("Lovelace");
        assertThat(request.phone()).isEqualTo("555-0100");
        assertThat(request.email()).isEqualTo("ada@example.com");
        assertThat(request.company()).isEqualTo("Analytical Engines");
        assertThat(request.jobTitle()).isEqualTo("Mathematician");
        assertThat(request.notes()).isEqualTo("Notes");
        assertThat(request).isEqualTo(equalRequest).hasSameHashCodeAs(equalRequest);
        assertThat(request.toString()).contains("Ada", "ada@example.com");
    }

    @Test
    void contactResponseExposesItsValuesAndRecordValueSemantics() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 2, 3, 4);
        LocalDateTime updatedAt = createdAt.plusDays(1);
        ContactResponse response = new ContactResponse(
                "contact-1", "Ada", "Lovelace", "555-0100", "ada@example.com",
                "Analytical Engines", "Mathematician", "Notes", true, createdAt, updatedAt);
        ContactResponse equalResponse = new ContactResponse(
                "contact-1", "Ada", "Lovelace", "555-0100", "ada@example.com",
                "Analytical Engines", "Mathematician", "Notes", true, createdAt, updatedAt);

        assertThat(response.id()).isEqualTo("contact-1");
        assertThat(response.firstName()).isEqualTo("Ada");
        assertThat(response.lastName()).isEqualTo("Lovelace");
        assertThat(response.phone()).isEqualTo("555-0100");
        assertThat(response.email()).isEqualTo("ada@example.com");
        assertThat(response.company()).isEqualTo("Analytical Engines");
        assertThat(response.jobTitle()).isEqualTo("Mathematician");
        assertThat(response.notes()).isEqualTo("Notes");
        assertThat(response.favorite()).isTrue();
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.updatedAt()).isEqualTo(updatedAt);
        assertThat(response).isEqualTo(equalResponse).hasSameHashCodeAs(equalResponse);
        assertThat(response.toString()).contains("contact-1", "Ada");
    }

    @Test
    void contactPageExposesItsValuesAndRecordValueSemantics() {
        ContactResponse contact = new ContactResponse(
                "contact-1", "Ada", null, "555-0100", null,
                null, null, null, false, LocalDateTime.MIN, LocalDateTime.MIN);
        ContactPage page = new ContactPage(List.of(contact), 0, 20, 1, 1);
        ContactPage equalPage = new ContactPage(List.of(contact), 0, 20, 1, 1);

        assertThat(page.content()).containsExactly(contact);
        assertThat(page.page()).isZero();
        assertThat(page.size()).isEqualTo(20);
        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(page.totalPages()).isEqualTo(1);
        assertThat(page).isEqualTo(equalPage).hasSameHashCodeAs(equalPage);
        assertThat(page.toString()).contains("totalElements=1");
    }

    @Test
    void favoriteRequestExposesValueAndRecordValueSemantics() {
        FavoriteRequest request = new FavoriteRequest(true);
        FavoriteRequest equalRequest = new FavoriteRequest(true);

        assertThat(request.favorite()).isTrue();
        assertThat(request).isEqualTo(equalRequest).hasSameHashCodeAs(equalRequest);
        assertThat(request.toString()).contains("favorite=true");
    }

    @Test
    void importJobResponseExposesValuesAndRecordValueSemantics() {
        ImportController.ImportJobResponse response =
                new ImportController.ImportJobResponse(7L, "COMPLETED");
        ImportController.ImportJobResponse equalResponse =
                new ImportController.ImportJobResponse(7L, "COMPLETED");

        assertThat(response.executionId()).isEqualTo(7L);
        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(response).isEqualTo(equalResponse).hasSameHashCodeAs(equalResponse);
        assertThat(response.toString()).contains("COMPLETED");
    }
}
