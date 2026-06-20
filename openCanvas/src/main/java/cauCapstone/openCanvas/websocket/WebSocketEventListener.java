package cauCapstone.openCanvas.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import cauCapstone.openCanvas.websocket.chatroom.SessionRepository;
import cauCapstone.openCanvas.websocket.chatroom.SubscribeRepository;

// 3분동안 웹소켓 연결이 끊겼다면(프론트에서 StompCommand로 끊긴 상태를 모를 때) 3분 유지한다면 그 유저는 문서방을 나간 것으로 간주함.
// 그 때 문서방을 나간 유저가 편집자인지, 일반 유저인지 따라 달라짐.
// 이 클래스는 오직 리스너를 통해 웹소켓 연결 끊김(disconnect) 상태를 감지하면 3분동안 TTL을 주는 키를 레디스에 저장하는 역할만 한다.
@Slf4j
@RequiredArgsConstructor
@Component
public class WebSocketEventListener {

    private final SessionRepository sessionRepository;
    private final SubscribeRepository subscribeRepository;

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();

        String subject = sessionRepository.getSubjectBySessionId(sessionId);
        String roomId =  subscribeRepository.getRoomIdBySubject(subject);

        if (roomId != null && subject != null) {
            log.info("DISCONNECT 발생: session={}, room={}, user={}", sessionId, roomId, subject);

            subscribeRepository.makeDisconnectKey(roomId, subject);
            
        }
        
    }
}
