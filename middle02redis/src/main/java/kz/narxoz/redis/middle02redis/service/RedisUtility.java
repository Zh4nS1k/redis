package kz.narxoz.redis.middle02redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RedisUtility {

    private final RedisTemplate<String, Object> redisTemplate;

    public void incrementPopularity(String key, Long memberId) {
        redisTemplate.opsForZSet().incrementScore(key, memberId.toString(), 1D);
    }

    public void removePopularity(String key, Long memberId) {
        redisTemplate.opsForZSet().remove(key, memberId.toString());
    }

    public List<Long> fetchPopularIds(String key, int limit) {
        int end = limit > 0 ? limit - 1 : -1;
        Set<Object> members = redisTemplate.opsForZSet().reverseRange(key, 0, end);
        if (members == null || members.isEmpty()) {
            return Collections.emptyList();
        }

        return members.stream()
                .map(Object::toString)
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }

    public Long getKeyTtl(String key, TimeUnit unit) {
        return redisTemplate.getExpire(key, unit);
    }

    public void deleteKeys(Set<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        redisTemplate.delete(keys);
    }

    public Set<String> scanKeys(String pattern) {
        return redisTemplate.keys(pattern);
    }
}
