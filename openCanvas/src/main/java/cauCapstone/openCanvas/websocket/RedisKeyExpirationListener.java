package cauCapstone.openCanvas.websocket;


import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import cauCapstone.openCanvas.websocket.chatroom.RemoveChatRoomService;
import cauCapstone.openCanvas.websocket.chatroom.RemoveEditorService;
import cauCapstone.openCanvas.websocket.chatroom.SubscribeRepository;
import cauCapstone.openCanvas.websocket.snapshot.SnapshotService;

/*
 연결 해제 및 편집자 락과 관련된 Redis 키 만료 이벤트를 처리한다.
 
 disconnect 키가 만료되면 재접속하지 않은 편집자의 문서방을 정리하고,
 lock:document 키가 먼저 만료되면 disconnect 키를 생성해 정리 절차를 시작한다(lock:document키 -> disconnect키)
 */
@Slf4j
@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    private final SubscribeRepository subscribeRepository;
    private final RemoveEditorService removeEditorService;
    private final SnapshotService snapshotService;
    private final RemoveChatRoomService removeChatRoomService;

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer,
                                      SubscribeRepository subscribeRepository,
                                      RemoveEditorService removeEditorService,
                                      SnapshotService snapshotService,
                                      RemoveChatRoomService removeChatRoomService) {
        super(listenerContainer);
        this.subscribeRepository = subscribeRepository;
        this.removeEditorService = removeEditorService;
        this.snapshotService = snapshotService;
        this.removeChatRoomService = removeChatRoomService;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();

        if (expiredKey.startsWith("disconnect:")) {
            String[] parts = expiredKey.split(":");
            if (parts.length < 3) {
                log.warn("disconnect 키 형식이 올바르지 않음: {}", expiredKey);
                return;
            }

            String roomId = parts[1];
            String subject = parts[2];

            String editorSubject = subscribeRepository.getEditorSubjectByRoomId(roomId);
            
            if (editorSubject == null) {
                log.warn("editorSubject 정보 없음. roomId: {}", roomId);
                return;
            }
            
            removeEditorService.removeEditorSubject(subject);

            if (subject.equals(editorSubject)) {
                log.warn("편집자 {}가 3분간 재연결하지 않아 문서방 {} 제거 시작", subject, roomId);

                snapshotService.saveSnapshotToDB(roomId);

                new Thread(() -> {
                    try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                    removeChatRoomService.removeChatRoom(roomId);
                }).start();

            } else {
            	// 편집자가 아닌경우 아무것도 할 필요가 없음.
            }
        }else if(expiredKey.startsWith("lock:document:")) {
        	
            String[] parts = expiredKey.split(":");
            if (parts.length < 3) {
                log.warn("lock 키 포맷 이상함: {}", expiredKey);
                return;
            }

            String roomId = parts[2];

            String editorSubject = subscribeRepository.getEditorSubjectByRoomId(roomId);
            if (editorSubject == null) {
                log.warn("락 만료 감지됨 - editor 정보 없음. roomId: {}", roomId);
                return;
            }
            
            subscribeRepository.makeDisconnectKey2(roomId, editorSubject);
            log.info("락 만료로 인해 disconnect 키 [{}] 강제로 만듦 → 삭제 결과: {}", editorSubject);
        }
    }
}
