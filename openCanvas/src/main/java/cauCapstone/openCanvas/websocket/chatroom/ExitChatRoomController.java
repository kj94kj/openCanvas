package cauCapstone.openCanvas.websocket.chatroom;

import cauCapstone.openCanvas.websocket.snapshot.SnapshotService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class ExitChatRoomController {

    private final SubscribeRepository subscribeRepository;
    private final RemoveEditorService removeEditorService;
    private final SnapshotService snapshotService;
    private final RemoveChatRoomService removeChatRoomService;

    @PostMapping("/exit")
    @Operation(
            summary = "문서방 나가기",
            description = """
                    WebSocket 연결을 종료한 후 호출합니다.
                    편집자가 나가면 현재 스냅샷을 DB에 저장하고 ROOMOUT 메시지를
                    발행한 뒤 문서방을 삭제합니다.
                    일반 참여자가 호출하면 문서방 종료 작업은 수행하지 않습니다.
                    """
    )
    public ResponseEntity<String> exitRoom(@RequestParam(name = "roomId") String roomId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body("인증되지 않은 사용자입니다.");
        }

        String subject = (String) auth.getPrincipal(); // JWT 인증 기반에서 subject (이메일 등) 추출

        String editorSubject = subscribeRepository.getEditorSubjectByRoomId(roomId);
        
        removeEditorService.removeEditorSubject(subject);
        
        if (editorSubject == null || !subject.equals(editorSubject)) {
        	log.debug("일반 참여자의 문서방 나가기: roomId={}, subject={}", roomId, subject);
            return ResponseEntity.ok("편집자가 아니므로 아무 작업도 하지 않음.");
        }

        log.info("편집자의 문서방 종료: roomId={}, subject={}", roomId, subject);

        subscribeRepository.removeDisconnectKey(roomId, subject);

        snapshotService.saveSnapshotToDB(roomId);

        removeChatRoomService.removeChatRoom(roomId);

        return ResponseEntity.ok("문서방 종료 처리 완료");
    }
}