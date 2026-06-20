package cauCapstone.openCanvas.websocket.chatroom;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/*
 WebSocket 세션 ID와 사용자 식별자(subject)의 매핑을 Redis에서 관리한다.
*/
@Service
@RequiredArgsConstructor
public class SessionRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String SESSION_PREFIX = "ws:session:";

    public void registerSession(String sessionId, String subject) {
        String key = SESSION_PREFIX + sessionId;
        redisTemplate.opsForValue().set(key, subject, Duration.ofDays(1));
    }

    public String getSubjectBySessionId(String sessionId) {
        String key = SESSION_PREFIX + sessionId;
        return redisTemplate.opsForValue().get(key);
    }

    public void removeSession(String sessionId) {
        String key = SESSION_PREFIX + sessionId;
        redisTemplate.delete(key);
    }
     
}
