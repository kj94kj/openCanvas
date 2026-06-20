package cauCapstone.openCanvas.websocket.chatroom;

import java.util.List;

import cauCapstone.openCanvas.rdb.dto.WritingDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "문서방 정보 응답")
public class ChatRoomDto {
	@Schema(description = "방id")
    private String roomId;
	@Schema(description = "글의 제목")
    private String name;
	@Schema(description = "편집자 email")
    private String subject;
    @Schema(
            description = "현재 글의 버전",
            example = "2.2.1"
    )

    private String version;	
    
	@Schema(description = "여태 썼던 글 모음")
    private List<WritingDto> writings;
    
    public static ChatRoomDto fromEntity(ChatRoomRedisEntity crre, List<WritingDto> w){
    	
    	return new ChatRoomDto(crre.getRoomId(), crre.getName(), crre.getSubject(), crre.getVersion(), w);
    }
    
}
