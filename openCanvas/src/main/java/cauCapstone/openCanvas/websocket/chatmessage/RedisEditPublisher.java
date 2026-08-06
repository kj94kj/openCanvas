package cauCapstone.openCanvas.websocket.chatmessage;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import cauCapstone.openCanvas.websocket.chatroom.EditAuthorityService;
import cauCapstone.openCanvas.websocket.chatroom.SessionRepository;
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
    private final SessionRepository sessionRepository;
    private final EditAuthorityService editAuthorityService;

    // subject는 문서방에서 유일한 편집자를 의미한다.
    // 편집 권한을 확인하고 락을 갱신하고, 편집 내용을 Redis에 저장한 후 메시지를 발행한다.
    public void editPublish(
            ChannelTopic topic,
            ChatMessage message,
            String sessionId
    ) {
        validateRequest(topic, message, sessionId);

        String subject =
                sessionRepository.getSubjectBySessionId(sessionId);

        if (!StringUtils.hasText(subject)) {
            throw new AccessDeniedException(
                    "WebSocket 세션의 사용자 정보를 찾을 수 없습니다."
            );
        }

        String roomId = message.getRoomId();

        // 편집자가 맞다면 락의 TTL을 갱신한다.
        editAuthorityService.requireEditorAndRefresh(
                roomId,
                subject
        );

        if (message.getMessage() == null) {
            return;
        }

        try {
            long timestamp = System.currentTimeMillis();
            message.setTimestamp(timestamp);

            SnapshotEntity snapshot =
                    SnapshotEntity.makeSnapshot(
                            roomId,
                            message.getMessage(),
                            message.getParagraphId(),
                            timestamp
                    );

            snapshotRepository.saveSnapshot(
                    roomId,
                    snapshot,
                    message.getAfterParagraphId()
            );

            redisTemplate.convertAndSend(
                    topic.getTopic(),
                    message
            );

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

    private void validateRequest(
            ChannelTopic topic,
            ChatMessage message,
            String sessionId
    ) {
        if (topic == null || !StringUtils.hasText(topic.getTopic())) {
            throw new IllegalArgumentException(
                    "Redis 발행 토픽이 없습니다."
            );
        }

        if (message == null) {
            throw new IllegalArgumentException(
                    "편집 메시지가 없습니다."
            );
        }

        if (!StringUtils.hasText(message.getRoomId())) {
            throw new IllegalArgumentException(
                    "편집 메시지에 roomId가 없습니다."
            );
        }

        if (!StringUtils.hasText(sessionId)) {
            throw new AccessDeniedException(
                    "WebSocket sessionId가 없습니다."
            );
        }
    }
}