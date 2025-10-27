package kz.narxoz.redis.middle02redis.dto;

import jakarta.validation.constraints.NotBlank;

public record SessionDataRequest(
        @NotBlank(message = "Payload cannot be empty")
        String payload
) {
}
