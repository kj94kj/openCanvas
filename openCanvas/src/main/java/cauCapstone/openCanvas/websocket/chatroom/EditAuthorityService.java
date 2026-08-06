package cauCapstone.openCanvas.websocket.chatroom;

import java.util.Objects;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class EditAuthorityService {

    private final ChatRoomRepository chatRoomRepository;
    private final SubscribeRepository subscribeRepository;

    public enum RoomRole {
        EDITOR,
        VIEWER
    }

    /**
     * 방 입장 시 호출한다.
     * 기존 편집자가 재입장했다면 락을 복구하고 disconnect 키를 제거한다.
     */
    public RoomRole enterRoom(String roomId, String subject) {
        validateArguments(roomId, subject);

        ChatRoomRedisEntity room = findRoom(roomId);

        // 현재 방의 편집자가 아닌 사용자는 관전자
        if (!Objects.equals(subject, room.getSubject())) {
            return RoomRole.VIEWER;
        }

        restoreOrExtendLock(roomId, subject);

        // 재접속이 확인됐으므로 유예 키 제거
        subscribeRepository.removeDisconnectKey(roomId, subject);

        return RoomRole.EDITOR;
    }

    /**
     * 편집 메시지를 발행할 때 호출한다.
     */
    public void requireEditorAndRefresh(String roomId, String subject) {
        validateArguments(roomId, subject);

        ChatRoomRedisEntity room = findRoom(roomId);

        if (!Objects.equals(subject, room.getSubject())) {
            throw new AccessDeniedException("현재 문서방의 편집자가 아닙니다.");
        }

        restoreOrExtendLock(roomId, subject);

    }

    private void restoreOrExtendLock(String roomId, String subject) {
        String lockOwner = subscribeRepository.getLockOwner(roomId);

        if (lockOwner == null) {
            subscribeRepository.setLock(roomId, subject);

            String createdLockOwner =
                    subscribeRepository.getLockOwner(roomId);

            if (!Objects.equals(subject, createdLockOwner)) {
                throw new AccessDeniedException(
                        "편집 락을 획득하지 못했습니다."
                );
            }

            return;
        }

        if (!Objects.equals(subject, lockOwner)) {
            throw new AccessDeniedException(
                    "다른 사용자가 현재 편집 중입니다."
            );
        }

        subscribeRepository.extendLock(roomId);
    }

    private ChatRoomRedisEntity findRoom(String roomId) {
        ChatRoomRedisEntity room =
                chatRoomRepository.findRoomById(roomId);

        if (room == null) {
            throw new IllegalStateException(
                    "문서방을 찾을 수 없습니다."
            );
        }

        return room;
    }

    private void validateArguments(String roomId, String subject) {
        if (!StringUtils.hasText(roomId)) {
            throw new IllegalArgumentException("roomId가 없습니다.");
        }

        if (!StringUtils.hasText(subject)) {
            throw new AccessDeniedException(
                    "사용자 정보를 확인할 수 없습니다."
            );
        }
    }
}