package kz.narxoz.redis.middle02redis.dto;

import java.util.List;

public record SessionInfo(
        String sessionId,
        String username,
        List<String> data,
        long ttlMinutes
) {
}
