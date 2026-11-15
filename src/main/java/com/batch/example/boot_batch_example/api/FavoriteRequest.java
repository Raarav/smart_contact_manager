package com.batch.example.boot_batch_example.api;

import jakarta.validation.constraints.NotNull;

public record FavoriteRequest(@NotNull Boolean favorite) {
}
