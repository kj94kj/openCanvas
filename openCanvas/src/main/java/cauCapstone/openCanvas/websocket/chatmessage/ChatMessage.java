package cauCapstone.openCanvas.websocket.chatmessage;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = """
		WebSocket을 통해 송수신하는 문서방 메시지 포맷
		메시지 유형
		- UPDATE: 문서방을 구독했을 때 이 타입으로 보냄.
		- EDIT: 문서 작성시 이 타입으로 보내야함.
		- ROOMOUT: 편집자 퇴장으로 인해 문서방이 종료 됬을때 이 타입으로 보냄.
		이 메시지를 받은 유저는 웹소켓 구독 해제를 하고, 연결을 끊으면 됩니다. 그리고 /api/room/exit로 컨트롤러를 호출해주세요.
		""")
public class ChatMessage {

    public enum MessageType {
        UPDATE, EDIT, ROOMOUT
    }
    
    private MessageType type; 
    private String roomId;
    private String subject;
    @Schema(description = "해당 블럭 내용")
    private String message; 
    @Schema(description = "문서 내 블럭(너무 많은 메시지를 보낼 것을 고려해서 블럭으로 나눔) 번호", example = "3")
    private String num;	
    
    private long timestamp;
    
    public ChatMessage(String roomId, MessageType type, String message) {
        this.roomId = roomId;
        this.type = type;
        this.message = message;
    }
    
    public ChatMessage(String roomId, MessageType type, String message, long timestamp) {
        this.roomId = roomId;
        this.type = type;
        this.message = message;
        this.timestamp = timestamp;
    }
    
    public ChatMessage(String roomId, MessageType type, String subject, String num) {
        this.roomId = roomId;
        this.type = type;
        this.subject = subject;
        this.message = num;
    }
}