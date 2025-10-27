package kz.narxoz.redis.middle02redis.api;

import kz.narxoz.redis.middle02redis.service.CacheMonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/cache")
@RequiredArgsConstructor
public class CacheController {

    private final CacheMonitorService cacheMonitorService;

    @GetMapping("/monitor")
    public Map<String, Long> monitorKeys(@RequestParam(defaultValue = "book:*") String pattern,
                                         @RequestParam(defaultValue = "SECONDS") TimeUnit unit) {
        return cacheMonitorService.collectKeyTtl(pattern, unit);
    }

    @PostMapping("/cleanup")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cleanup(@RequestParam(defaultValue = "book:*") String pattern) {
        cacheMonitorService.cleanupKeys(pattern);
    }
}
