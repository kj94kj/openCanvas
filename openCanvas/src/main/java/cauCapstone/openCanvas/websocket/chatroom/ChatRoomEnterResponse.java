package cauCapstone.openCanvas.websocket.chatroom;

import java.util.List;

import cauCapstone.openCanvas.websocket.snapshot.ParagraphSnapshotDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatRoomEnterResponse {

    private ChatRoomDto room;
    private String role;
    private List<ParagraphSnapshotDto> snapshots;
}