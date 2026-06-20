package cauCapstone.openCanvas.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import cauCapstone.openCanvas.jwt.JwtTokenizer;
import cauCapstone.openCanvas.websocket.chatmessage.ChatMessage;
import cauCapstone.openCanvas.websocket.chatmessage.RedisPublisher;
import cauCapstone.openCanvas.websocket.chatroom.SessionRepository;
import cauCapstone.openCanvas.websocket.chatroom.SubscribeRepository;
import io.jsonwebtoken.Claims;

@Slf4j
@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor{
	
	private final JwtTokenizer jwtTokenizer;
    private final SessionRepository sessionRegistryService;
    private final SubscribeRepository subscribeRegistryService;
	private final RedisPublisher redisPublisher;
	
	// stompcommand 상태를 읽고 동작 처리 후에 메시지를 발행한다.
	@Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        
        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey();
        
        // 토큰은 connect 상태에서만 꺼내고 나중에는 유저정보는 sessionId를 이용해 redis에서 참조하도록함.
        if (StompCommand.CONNECT == accessor.getCommand()) {

            try {
            	String rawToken = accessor.getFirstNativeHeader("token");
            	if (rawToken != null && rawToken.startsWith("Bearer ")) {
            	    rawToken = rawToken.substring(7);
            	}

            	if (rawToken != null && !rawToken.isBlank()) {
            	    Claims claims = jwtTokenizer.verifySignature(rawToken, base64EncodedSecretKey);
            	    String subject = claims.getSubject();
            	    String sessionId = accessor.getSessionId();
            	    
            	    sessionRegistryService.registerSession(sessionId, subject);
            	}
            } catch (Exception e) {
                log.error("Error while verifying token", e);
            }

        }else if (StompCommand.SUBSCRIBE == accessor.getCommand()) {
        	
        	String destination = accessor.getDestination();
            String roomId = extractRoomId(destination);
        	
        	String sessionId = accessor.getSessionId();
        	String subject = sessionRegistryService.getSubjectBySessionId(sessionId);
        	
        	subscribeRegistryService.registerSubscribe(roomId, sessionId, subject);
        	
        	ChatMessage updateMessage = new ChatMessage();
        	updateMessage.setType(ChatMessage.MessageType.UPDATE);
        	updateMessage.setRoomId(roomId);
        	updateMessage.setSubject(subject);
        	updateMessage.setMessage(subject+" 유저가 "+roomId+"를 구독함.");
        
            // 재접속한 사용자라면 disconnectKey를 삭제한다.
        	subscribeRegistryService.removeDisconnectKey(roomId, subject);

            log.debug(updateMessage.getMessage()+" 메시지 전송을 시도함.");
            redisPublisher.publish(new ChannelTopic(roomId), updateMessage);

        }else if (StompCommand.UNSUBSCRIBE == accessor.getCommand()) {
        	String sessionId = accessor.getSessionId();
        	
        	String subject = sessionRegistryService.getSubjectBySessionId(sessionId);
        	String roomId = subscribeRegistryService.getRoomIdBySubject(subject);
        	
        	if(roomId != null && subject != null) {

            	ChatMessage updateMessage = new ChatMessage();
            	updateMessage.setType(ChatMessage.MessageType.UPDATE);
            	updateMessage.setRoomId(roomId);
            	updateMessage.setSubject(subject);
            	updateMessage.setMessage(subject+" 유저가 "+roomId+"를 구독 해제함.");

            	redisPublisher.publish(new ChannelTopic(roomId), updateMessage);
        	}else {
        		
        		if(subject != null) {
            		subscribeRegistryService.removeSuscribe(subject);
        		}
        	}
        	
            sessionRegistryService.removeSession(sessionId);
       
        }else if (StompCommand.DISCONNECT == accessor.getCommand()) {
        	String sessionId = accessor.getSessionId();
        	
        	String subject = sessionRegistryService.getSubjectBySessionId(sessionId);
        	String roomId = subscribeRegistryService.getRoomIdBySubject(subject);
        	
        	if(roomId != null && subject != null) {

            	ChatMessage updateMessage = new ChatMessage();
            	updateMessage.setType(ChatMessage.MessageType.UPDATE);
            	updateMessage.setRoomId(roomId);
            	updateMessage.setSubject(subject);
            	updateMessage.setMessage(subject+" 유저가 "+roomId+"를 구독 해제함.");

            	redisPublisher.publish(new ChannelTopic(roomId), updateMessage);
        	}else {
        		
        		if(subject != null) {
            		subscribeRegistryService.removeSuscribe(subject);
        		}
        	}
        	
            sessionRegistryService.removeSession(sessionId);
        }
        
        return message;
        
    }
	
    // /sub/chat/room/{roomId}는 토픽이기 때문에 {roomId}를 추출함.
	private String extractRoomId(String destination) {
	    // 예: /sub/chat/room/1234
	    if (destination == null) return null;

	    String[] parts = destination.split("/");
	    if (parts.length >= 5) {
	        return parts[4];
	    }
	    return null;
	}
}
