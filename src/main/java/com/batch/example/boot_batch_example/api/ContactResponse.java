package com.batch.example.boot_batch_example.api;

import java.time.LocalDateTime;

public record ContactResponse(
        String id,
        String firstName,
        String lastName,
        String phone,
        String email,
        String company,
        String jobTitle,
        String notes,
        boolean favorite,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
