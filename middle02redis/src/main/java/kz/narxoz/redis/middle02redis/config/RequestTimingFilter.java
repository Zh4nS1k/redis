package kz.narxoz.redis.middle02redis.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class RequestTimingFilter extends OncePerRequestFilter {

    private final long slowThresholdMs;

    public RequestTimingFilter(@Value("${http.logging.slow-threshold-ms:10}") long slowThresholdMs) {
        this.slowThresholdMs = slowThresholdMs;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long startNs = System.nanoTime();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationNs = System.nanoTime() - startNs;
            double durationMs = durationNs / 1_000_000.0;
            response.setHeader("X-Response-Time-Millis", String.format("%.3f", durationMs));

            String uri = buildUri(request);
            String message = String.format("%s %s -> %d in %.3f ms",
                    request.getMethod(),
                    uri,
                    response.getStatus(),
                    durationMs);

            if (durationMs > slowThresholdMs) {
                log.warn("[SLOW] {}", message);
            } else {
                log.info(message);
            }
        }
    }

    private String buildUri(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        if (StringUtils.hasText(query)) {
            return uri + "?" + query;
        }
        return uri;
    }
}
