package cauCapstone.openCanvas.websocket.snapshot;

import java.time.LocalDateTime;
import java.util.Comparator;
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
 * 글 작성을 마치면 블록별 최신 스냅샷을 가져와 최종적인 Writing으로 저장한다.
 * 신규 참여자에게는 현재 문서 상태를 복원할 수 있도록 블록별 스냅샷을 합쳐서 메시지 형태로 제공한다.
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
        
        Integer parentSiblingIndex = (version.size() > 2) ? version.get(2) : null;

        List<SnapshotEntity> snapshots = snapshotRepository.getAllSnapshots(roomId);
        if (snapshots == null || snapshots.isEmpty()) {
            throw new IllegalStateException("스냅샷이 존재하지 않습니다: " + roomId);
        }
        
        snapshots.sort(Comparator.comparingInt(s -> Integer.parseInt(s.getNum())));
        
        String fullBody = snapshots.stream().map(SnapshotEntity::getBody).collect(Collectors.joining());
        
        LocalDateTime time = LocalDateTime.now();

        WritingDto writingDto = new WritingDto(version.get(0), version.get(1), parentSiblingIndex, 
        		fullBody, time, room.getSubject(), room.getName());

        writingService.saveWriting(writingDto);

        snapshotRepository.deleteAllSnapshots(roomId);
    }
    
    public List<Integer> getIntVersion(String version){
        if (version == null || version.isEmpty()) {
            return List.of(); // 또는 예외 던지기
        }
        
        return List.of(version.split("\\.")).stream()
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
    
    public List<ChatMessage> giveSnapshot(String roomId) {
        ChatRoomRedisEntity room = chatRoomRepository.findRoomById(roomId);
        if (room == null) {
            throw new IllegalArgumentException("존재하지 않는 문서방입니다: " + roomId);
        }
        
        List<SnapshotEntity> snapshots = snapshotRepository.getAllSnapshots(roomId);
        
        if (snapshots == null || snapshots.isEmpty()) {
            return List.of();
        }

        snapshots.sort(Comparator.comparingInt(s -> Integer.parseInt(s.getNum())));

        return snapshots.stream()
            .map(snapshot -> {
                ChatMessage message = new ChatMessage();
                message.setType(ChatMessage.MessageType.EDIT);
                message.setRoomId(roomId);
                message.setSubject(room.getSubject()); 
                message.setMessage(snapshot.getBody());
                message.setNum(snapshot.getNum());     
                return message;
            })
            .collect(Collectors.toList());
    }
}
