package cauCapstone.openCanvas.websocket.chatroom;

import java.time.Duration;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 문서방 구독자, 연결 해제 유예 상태, 편집 락을 Redis에서 관리한다.
*/
@Slf4j
@RequiredArgsConstructor
@Service
public class SubscribeRepository {	
	
    private final RedisTemplate<String, String> redisTemplate;
    private static final String SESSION_PREFIX = "ws:subscribe:";
    private static final String DISCONNECT_PREFIX = "disconnect";
    private static final String LOCK_PREFIX = "lock:document:";
    
    public void registerEditorSubject(String roomId, String subject) {
    	String key = SESSION_PREFIX + "room:" +roomId + ":editorSubject";
    	
        redisTemplate.opsForValue().set(key, subject, Duration.ofDays(1));
    }
    
    public void registerSubscribe(String roomId, String sessionId, String subject) {
    	String key1 = SESSION_PREFIX + "room:" + roomId + ":subject";
    	String key2 = SESSION_PREFIX + "subject:" + subject + ":roomId";
    	
        redisTemplate.opsForSet().add(key1, subject);
        redisTemplate.expire(key1, Duration.ofDays(1));
        redisTemplate.opsForValue().set(key2, roomId, Duration.ofDays(1));
    }
    
    public Set<String> getSubjectsByRoomId(String roomId) {
    	String key= SESSION_PREFIX + "room:" + roomId + ":subject";
        return redisTemplate.opsForSet().members(key);
    }
    
    public String getRoomIdBySubject(String subject) {
    	String key = SESSION_PREFIX + "subject:" + subject + ":roomId";
        return redisTemplate.opsForValue().get(key);
    }
    
    public String getEditorSubjectByRoomId(String roomId) {
    	String key = SESSION_PREFIX +"room:"+ roomId + ":editorSubject";
    	return redisTemplate.opsForValue().get(key);
    }
    
    public void removeSuscribe(String subject) {
    	String key1 = SESSION_PREFIX + "subject:" + subject + ":roomId";
    	String roomId = redisTemplate.opsForValue().get(key1);
    	String key2 = SESSION_PREFIX + "room:" + roomId + ":subject";
    	
        redisTemplate.delete(key1);
        if (roomId != null) {
            redisTemplate.opsForSet().remove(key2, subject);
        }
    }
    
    public void removeEditorSubjectKey(String roomId) {
        String key = SESSION_PREFIX + "room:" + roomId + ":editorSubject";
        redisTemplate.delete(key);
    }
    
    // 3분동안 안들어오면(DISCONNECT) 나갔다고 판단하고, 편집자 권한 회수함.
    
    private String getDisconnectKey(String roomId, String subject) {
        return DISCONNECT_PREFIX + ":" + roomId + ":" + subject;
    }
    
    public void makeDisconnectKey(String roomId, String subject) {
        String key = getDisconnectKey(roomId, subject);
        redisTemplate.opsForValue().set(key, "pending", Duration.ofMinutes(3));
    }
    
    public void makeDisconnectKey2(String roomId, String subject) {
        String key = getDisconnectKey(roomId, subject);
        redisTemplate.opsForValue().set(key, "pending", Duration.ofMinutes(1));
    }
    
    public void removeDisconnectKey(String roomId, String subject) {
        String key = getDisconnectKey(roomId, subject);
        redisTemplate.delete(key);
    }
    
    // 편집자 락 키
    // 30분동안 동작 없으면 편집자 락 키를 회수함.
    
    private String getLockKey(String roomId) {
        return LOCK_PREFIX + roomId;
    }

    public String getLockOwner(String roomId) {
        return redisTemplate.opsForValue().get(getLockKey(roomId));
    }

    public void setLock(String roomId, String subject) {
        redisTemplate.opsForValue().set(getLockKey(roomId), subject, Duration.ofMinutes(30));
    }

    public void extendLock(String roomId, String subject) {
        redisTemplate.opsForValue().set(getLockKey(roomId), subject, Duration.ofMinutes(30));
    }
    
    public void removeLockKey(String roomId) {
        redisTemplate.delete(getLockKey(roomId));
    }
    
}
