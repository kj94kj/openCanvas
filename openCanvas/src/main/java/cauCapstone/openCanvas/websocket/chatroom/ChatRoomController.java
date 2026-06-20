package cauCapstone.openCanvas.websocket.chatroom;

import cauCapstone.openCanvas.rdb.dto.WritingDto;
import cauCapstone.openCanvas.rdb.service.WritingService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final WritingService writingService;
    private final ChatRoomService chatRoomService;
    // private final SnapshotService snapshotService;

    @PostMapping("/{roomId}/create")
    @Operation(
            summary = "문서방 생성",
            description = """
                    편집자가 문서방을 생성하고 기존 글 이력을 조회합니다.
                    최초 작성인 경우 title만 전달하며 버전은 1.1로 생성됩니다.
                    이어쓰기인 경우 부모 글의 depth와 siblingIndex가 필요합니다.
                    생성 이후 별도로 WebSocket에 연결해야 합니다.
                    """
    )
    public ResponseEntity<?> createRoomAndEnter(@RequestBody WritingDto writingDto,
            										@PathVariable(name = "roomId") String roomId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    	
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인되지 않음");
        }

        String email = (String) auth.getPrincipal();
        
        String version;
        
        if(writingDto.getDepth() == 0) {
        	version = "1.1";
        }else {
            int siblingIndex = writingService.checkWriting(
            		writingDto.getDepth(),
            		writingDto.getSiblingIndex(),
            		writingDto.getTitle()
            );

            version = (writingDto.getDepth() + 1) + "." + siblingIndex + "." + writingDto.getSiblingIndex();
        }

        ChatRoomRedisEntity chatRoom = chatRoomService.createChatRoom(
        	roomId,
            writingDto.getTitle(),
            email,
            version
        );

        List<WritingDto> history = writingService.getWritingsWithRoomId(chatRoom.getRoomId());

        ChatRoomDto chatRoomDto = ChatRoomDto.fromEntity(chatRoom, history);

        return ResponseEntity.ok(chatRoomDto);
    }
    
    @GetMapping("/{roomId}/enter")
    @Operation(
            summary = "문서방 참여 정보 조회",
            description = """
                    구독자로 참여할 문서방의 정보와 기존 글 이력을 조회합니다.
                    조회 이후 별도로 WebSocket에 연결하고 문서방 토픽을 구독해야 합니다.
                    """
    )
    public ResponseEntity<?> enterRoomAsSubscriber(@PathVariable(name = "roomId") String roomId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인되지 않음");
        }

        ChatRoomRedisEntity chatRoom = chatRoomService.findRoomById(roomId);
        
        if (chatRoom == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("존재하지 않는 roomId입니다.");
        }

        List<WritingDto> history = writingService.getWritingsWithRoomId(roomId);
        

        // TODO: 블록 단위 편집 구현 시 저장된 스냅샷을 입장 응답에 포함한다.
        // 현재는 블럭번호 1만 사용함. 따라서 작성중인 문서의 전체 내용을 전송하므로 별도의 스냅샷 조회가 필요하지 않다.
        // List<ChatMessage> snapshotList = snapshotService.giveSnapshot(roomId);

        ChatRoomDto chatRoomDto = ChatRoomDto.fromEntity(chatRoom, history); 

        return ResponseEntity.ok(chatRoomDto);
    }
}