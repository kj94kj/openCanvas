package cauCapstone.openCanvas.websocket.chatroom;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatRoomEnterResponse {

    private ChatRoomDto room;
    private String role;
}