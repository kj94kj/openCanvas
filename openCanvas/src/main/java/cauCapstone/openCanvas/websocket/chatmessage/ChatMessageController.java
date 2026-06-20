package cauCapstone.openCanvas.websocket.chatmessage;

import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.Message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Controller
@Slf4j
public class ChatMessageController {

	private final RedisEditPublisher redisPublisher;


    /*
     클라이언트가 /pub/chat/message로 전송한 문서 편집 메시지를 처리한다.
     편집 권한 검증과 메시지 배포는 RedisEditPublisher가 담당한다.
    */
    @MessageMapping("/chat/message")
    public void message(Message<?> rawMessage, ChatMessage message) {
    	
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(rawMessage);
        String sessionId = accessor.getSessionId();

        // sessionId를 통해 현재 사용자의 문서 편집 권한을 확인한다.
        if(ChatMessage.MessageType.EDIT.equals(message.getType())) {
            // 문서편집, 작성을 할 때는 프론트에서 messageType를 EDIT으로 지정해야함.
            redisPublisher.editPublish(new ChannelTopic(message.getRoomId()), message, sessionId);
            
            
        }else {
            log.warn(
                    "허용되지 않은 WebSocket 메시지 타입: type={}, sessionId={}",
                    message.getType(),
                    sessionId
            );
        	
            throw new IllegalArgumentException("허용되지 않은 메시지 타입");
        }
     
    }
}