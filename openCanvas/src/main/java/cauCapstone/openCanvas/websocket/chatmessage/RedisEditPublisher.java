package cauCapstone.openCanvas.websocket.chatmessage;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import cauCapstone.openCanvas.websocket.chatroom.ChatRoomRedisEntity;
import cauCapstone.openCanvas.websocket.chatroom.ChatRoomRepository;
import cauCapstone.openCanvas.websocket.chatroom.SessionRepository;
import cauCapstone.openCanvas.websocket.chatroom.SubscribeRepository;
import cauCapstone.openCanvas.websocket.snapshot.SnapshotEntity;
import cauCapstone.openCanvas.websocket.snapshot.SnapshotRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class RedisEditPublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SnapshotRepository snapshotRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final SessionRepository sessionRepository;
    private final SubscribeRepository subscribeRepository;

    // subject는 문서방에서 유일한 편집자를 의미한다.
    // 편집 권한을 확인하고 락을 갱신하고, 편집 내용을 Redis에 저장한 후 메시지를 발행한다.
    public void editPublish(ChannelTopic topic, ChatMessage message, String sessionId) {

        String subject = sessionRepository.getSubjectBySessionId(sessionId);
        String roomId = message.getRoomId();

        String lockOwner = subscribeRepository.getLockOwner(roomId);

        if (lockOwner == null) {

            ChatRoomRedisEntity room = chatRoomRepository.findRoomById(roomId);

            if (room == null) {
                throw new IllegalStateException("문서방을 찾을 수 없습니다.");
            }

            String editorSubject = room.getSubject();

            if (!subject.equals(editorSubject)) {
                throw new AccessDeniedException("편집자만 락을 생성할 수 있습니다.");
            }

            subscribeRepository.setLock(roomId, subject);
            subscribeRepository.removeDisconnectKey(roomId, subject);

        } else {

            if (!lockOwner.equals(subject)) {
                throw new AccessDeniedException("다른 사용자가 현재 편집 중입니다.");
            }

            // 편집이 계속되는 동안 락이 만료되지 않도록 TTL을 갱신한다.
            subscribeRepository.extendLock(roomId);
        }

        try {
            if (message.getMessage() == null) {
                return;
            }

            message.setTimestamp(System.currentTimeMillis());

            SnapshotEntity snapshot = SnapshotEntity.makeSnapshot(
                    roomId,
                    message.getMessage(),
                    message.getParagraphId(),
                    message.getTimestamp()
            );

            snapshotRepository.saveSnapshot(
                    roomId,
                    snapshot,
                    message.getAfterParagraphId()
            );

            redisTemplate.convertAndSend(topic.getTopic(), message);

        } catch (Exception e) {
            log.error(
                    "편집 내용 저장 또는 발행 실패: roomId={}, subject={}",
                    roomId,
                    subject,
                    e
            );

            throw new IllegalStateException(
                    "편집 내용을 저장하거나 발행하지 못했습니다.",
                    e
            );
        }
    }
}