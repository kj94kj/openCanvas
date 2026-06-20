package cauCapstone.openCanvas.websocket.chatroom;

import java.util.List;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

/*
 Redis Hash에 저장된 문서방 정보를 관리한다.
 */
@RequiredArgsConstructor
@Repository
public class ChatRoomRepository {
 
    private static final String CHAT_ROOMS = "CHAT_ROOM";
    private static final String LOCK_PREFIX = "lock:document:";
    
    private final RedisTemplate<String, Object> redisTemplate; 
    private HashOperations<String, String, ChatRoomRedisEntity> opsHashChatRoom;

    @PostConstruct
    private void init() {
        opsHashChatRoom = redisTemplate.opsForHash();
    }

    public List<ChatRoomRedisEntity> findAllRoom() {
        return opsHashChatRoom.values(CHAT_ROOMS);
    }

    public ChatRoomRedisEntity findRoomById(String id) {
        return opsHashChatRoom.get(CHAT_ROOMS, id);
    }
    

    public void createRoom(ChatRoomRedisEntity room) {
        opsHashChatRoom.put(CHAT_ROOMS, room.getRoomId(), room);
    }
    
    public void deleteRoom(String roomId) {
        opsHashChatRoom.delete(CHAT_ROOMS, roomId);
    }
    
    public void deleteLock(String roomId) {
        redisTemplate.delete(LOCK_PREFIX + roomId);
    }
}