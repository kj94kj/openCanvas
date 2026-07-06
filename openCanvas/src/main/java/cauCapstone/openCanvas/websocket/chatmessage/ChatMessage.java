package cauCapstone.openCanvas.websocket.chatmessage;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "문서 내 문단별로 부여하는 id, 전송을 문서 전체가 아닌 문단별로 해서 전송량을 줄임.")
    private String paragraphId;	
    @Schema(description = "새 문단을 삽입할 때 기준이 되는 앞 문단 id")
    private String afterParagraphId;
    private long timestamp;
    
}