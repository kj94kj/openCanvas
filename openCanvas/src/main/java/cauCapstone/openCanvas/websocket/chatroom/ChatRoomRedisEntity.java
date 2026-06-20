package cauCapstone.openCanvas.websocket.chatroom;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatRoomRedisEntity implements Serializable {

    private static final long serialVersionUID = 6494678977089006639L;

    private String roomId;
    private String name;
    private String subject; // 편집자의 이메일
    private String version;	// 현재 Writing 버전을 string화 해서 넣어야함.
    
    public static ChatRoomRedisEntity create(String roomId, String name, String subject, String version) {
        ChatRoomRedisEntity chatRoom = new ChatRoomRedisEntity();
        chatRoom.roomId = roomId;
        chatRoom.name = name;
        chatRoom.subject = subject;
        chatRoom.version = version;
        return chatRoom;
    }
}