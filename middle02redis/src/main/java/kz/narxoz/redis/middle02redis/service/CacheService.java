package kz.narxoz.redis.middle02redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CacheService {
    private final RedisTemplate<String, Object> redisTemplate;

    public void cacheObject(String key, Object object, long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, object, timeout, timeUnit);
    }

    public Object getCacheObject(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public <T> T getCacheObject(String key, Class<T> type) {
        Object cached = getCacheObject(key);
        if (cached == null) {
            return null;
        }
        if (type.isInstance(cached)) {
            return type.cast(cached);
        }
        return null;
    }

    public void deleteCachedObject(String key) {
        redisTemplate.delete(key);
    }
}
