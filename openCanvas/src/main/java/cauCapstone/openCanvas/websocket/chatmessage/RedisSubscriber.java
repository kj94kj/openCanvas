package cauCapstone.openCanvas.websocket.chatmessage;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessageSendingOperations messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {

            String publishMessage = (String) redisTemplate.getStringSerializer().deserialize(message.getBody());
            
            ChatMessage roomMessage = objectMapper.readValue(publishMessage, ChatMessage.class);

            log.debug("Sending message to room: " + roomMessage.getRoomId());
            messagingTemplate.convertAndSend("/sub/chat/room/" + roomMessage.getRoomId(), roomMessage);
        } catch (Exception e) {		
            log.error("Redis 메시지 처리 실패", e);
        }
    }
}
