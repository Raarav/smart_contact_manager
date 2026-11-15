package com.batch.example.boot_batch_example.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactRequest(
        @NotBlank @Size(max = 100) String firstName,
        @Size(max = 100) String lastName,
        @NotBlank @Size(max = 32) String phone,
        @Email @Size(max = 254) String email,
        @Size(max = 200) String company,
        @Size(max = 200) String jobTitle,
        @Size(max = 4000) String notes) {
}
