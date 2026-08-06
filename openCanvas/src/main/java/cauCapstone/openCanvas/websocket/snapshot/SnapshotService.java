package cauCapstone.openCanvas.websocket.snapshot;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import cauCapstone.openCanvas.rdb.dto.WritingDto;
import cauCapstone.openCanvas.rdb.service.WritingService;
import cauCapstone.openCanvas.websocket.chatmessage.ChatMessage;
import cauCapstone.openCanvas.websocket.chatroom.ChatRoomRedisEntity;
import cauCapstone.openCanvas.websocket.chatroom.ChatRoomRepository;
import lombok.RequiredArgsConstructor;

/**
 * 글 작성을 마치면 문단별 최신 스냅샷을 가져와 최종적인 Writing으로 저장한다.
 * 신규 참여자에게는 현재 문서 상태를 복원할 수 있도록 문단별 스냅샷을 메시지 형태로 제공한다.
 */
@Service
@RequiredArgsConstructor
public class SnapshotService {

    private final SnapshotRepository snapshotRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final WritingService writingService;

    public void saveSnapshotToDB(String roomId) {
        ChatRoomRedisEntity room = chatRoomRepository.findRoomById(roomId);

        if (room == null) {
            throw new IllegalArgumentException("존재하지 않는 문서방입니다: " + roomId);
        }

        List<Integer> version = getIntVersion(room.getVersion());

        if (version.size() < 2) {
            throw new IllegalStateException("잘못된 버전 형식입니다: " + room.getVersion());
        }

        Integer parentSiblingIndex = (version.size() > 2) ? version.get(2) : null;

        List<SnapshotEntity> snapshots = snapshotRepository.findSnapshots(roomId);

        if (snapshots == null || snapshots.isEmpty()) {
            throw new IllegalStateException("스냅샷이 존재하지 않습니다: " + roomId);
        }

        String fullBody = snapshots.stream()
            .map(SnapshotEntity::getBody)
            .collect(Collectors.joining("\n"));

        LocalDateTime time = LocalDateTime.now();

        WritingDto writingDto = new WritingDto(
            version.get(0),
            version.get(1),
            parentSiblingIndex,
            fullBody,
            time,
            room.getSubject(),
            room.getName()
        );

        writingService.saveWriting(writingDto);

        snapshotRepository.deleteSnapshots(roomId);
    }

    public List<Integer> getIntVersion(String version) {
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("버전 정보가 비어 있습니다.");
        }

        return List.of(version.split("\\."))
            .stream()
            .map(Integer::parseInt)
            .collect(Collectors.toList());
    }

    public List<ChatMessage> giveSnapshot(String roomId) {
        ChatRoomRedisEntity room = chatRoomRepository.findRoomById(roomId);

        if (room == null) {
            throw new IllegalArgumentException("존재하지 않는 문서방입니다: " + roomId);
        }

        List<SnapshotEntity> snapshots = snapshotRepository.findSnapshots(roomId);

        if (snapshots == null || snapshots.isEmpty()) {
            return List.of();
        }

        return snapshots.stream()
            .map(snapshot -> {
                ChatMessage message = new ChatMessage();
                message.setType(ChatMessage.MessageType.EDIT);
                message.setRoomId(roomId);
                message.setSubject(room.getSubject());
                message.setMessage(snapshot.getBody());

                // num 대신 paragraphId 사용
                message.setParagraphId(snapshot.getParagraphId());

                return message;
            })
            .collect(Collectors.toList());
    }
    
    public List<ParagraphSnapshotDto> getRoomEntrySnapshots(
            String roomId
    ) {
        List<SnapshotEntity> snapshots =
                snapshotRepository.findSnapshots(roomId);

        if (snapshots == null || snapshots.isEmpty()) {
            return List.of();
        }

        List<ParagraphSnapshotDto> result =
                new java.util.ArrayList<>(snapshots.size());

        for (int i = 0; i < snapshots.size(); i++) {
            SnapshotEntity snapshot = snapshots.get(i);

            String afterParagraphId =
                    i == 0
                            ? null
                            : snapshots.get(i - 1)
                                    .getParagraphId();

            result.add(
                    new ParagraphSnapshotDto(
                            snapshot.getParagraphId(),
                            snapshot.getBody() == null
                                    ? ""
                                    : snapshot.getBody(),
                            afterParagraphId
                    )
            );
        }

        return result;
    }
}