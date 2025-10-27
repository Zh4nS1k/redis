package kz.narxoz.redis.middle02redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CacheMonitorService {

    private final RedisUtility redisUtility;
    @Value("${cache.monitor.enabled:true}")
    private boolean monitorEnabled;

    @Scheduled(fixedDelayString = "${cache.monitor.delay:60000}")
    public void autoCleanup() {
        if (!monitorEnabled) {
            return;
        }
        cleanupKeys("book:*");
        cleanupKeys("books:popular");
    }

    public Map<String, Long> collectKeyTtl(String pattern, TimeUnit unit) {
        Set<String> keys = Optional.ofNullable(redisUtility.scanKeys(pattern)).orElse(Collections.emptySet());
        return keys.stream()
                .collect(Collectors.toMap(Function.identity(), key -> {
                    Long ttl = redisUtility.getKeyTtl(key, unit);
                    return ttl != null ? ttl : -1L;
                }));
    }

    public void cleanupKeys(String pattern) {
        Set<String> keys = redisUtility.scanKeys(pattern);
        if (keys == null || keys.isEmpty()) {
            return;
        }
        Set<String> expired = keys.stream()
                .filter(key -> {
                    Long ttl = redisUtility.getKeyTtl(key, TimeUnit.SECONDS);
                    return ttl != null && ttl <= 0;
                })
                .collect(Collectors.toSet());
        redisUtility.deleteKeys(expired);
    }
}
