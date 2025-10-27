package kz.narxoz.redis.middle02redis.service;

import jakarta.servlet.http.HttpSession;
import kz.narxoz.redis.middle02redis.dto.SessionInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SessionService {

    private static final String USER_ATTRIBUTE = "session:user";
    private static final String DATA_ATTRIBUTE = "session:dataList";

    private final RedisUtility redisUtility;

    @Value("${spring.session.redis.namespace:spring:session}")
    private String sessionNamespace;

    public SessionInfo login(HttpSession session, String username) {
        session.setAttribute(USER_ATTRIBUTE, username);
        return buildSessionInfo(session);
    }

    public SessionInfo appendData(HttpSession session, String payload) {
        List<String> data = getOrCreateDataList(session);
        data.add(payload);
        session.setAttribute(DATA_ATTRIBUTE, data);
        return buildSessionInfo(session);
    }

    public SessionInfo getSessionInfo(HttpSession session) {
        return buildSessionInfo(session);
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }

    @SuppressWarnings("unchecked")
    private List<String> getOrCreateDataList(HttpSession session) {
        Object data = session.getAttribute(DATA_ATTRIBUTE);
        if (data instanceof List<?>) {
            return (List<String>) data;
        }
        List<String> dataList = new ArrayList<>();
        session.setAttribute(DATA_ATTRIBUTE, dataList);
        return dataList;
    }

    private SessionInfo buildSessionInfo(HttpSession session) {
        List<String> snapshot = new ArrayList<>(getOrCreateDataList(session));
        String username = (String) session.getAttribute(USER_ATTRIBUTE);
        long ttlMinutes = resolveTtlMinutes(session);
        return new SessionInfo(session.getId(), username, snapshot, ttlMinutes);
    }

    private long resolveTtlMinutes(HttpSession session) {
        Long ttl = redisUtility.getKeyTtl(buildRedisSessionKey(session.getId()), TimeUnit.MINUTES);
        if (ttl == null || ttl < 0) {
            return TimeUnit.SECONDS.toMinutes(session.getMaxInactiveInterval());
        }
        return ttl;
    }

    private String buildRedisSessionKey(String sessionId) {
        String prefix = sessionNamespace.endsWith(":") ? sessionNamespace : sessionNamespace + ":";
        return prefix + "sessions:" + sessionId;
    }
}
