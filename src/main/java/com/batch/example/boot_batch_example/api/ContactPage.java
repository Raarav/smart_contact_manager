package com.batch.example.boot_batch_example.api;

import java.util.List;

public record ContactPage(
        List<ContactResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {
}
