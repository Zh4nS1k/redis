package kz.narxoz.redis.middle02redis.dto;

import jakarta.validation.constraints.NotBlank;

public record SessionLoginRequest(
        @NotBlank(message = "Username is required")
        String username
) {
}
