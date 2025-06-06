package cauCapstone.openCanvas.rdb.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import cauCapstone.openCanvas.rdb.dto.ContentDto;
import cauCapstone.openCanvas.rdb.dto.WritingDto;
import cauCapstone.openCanvas.rdb.service.ContentService;
import cauCapstone.openCanvas.rdb.service.WritingService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/writings")
public class WritingController {

    private final WritingService writingService;

    // 다른 메소드에서 써야함.
    @PostMapping("/check")
    @Operation(summary = "글 작성 가능 여부 확인", description = "현재 depth의 하위에 글을 더 작성할 수 있는지 확인하고, 가능한 siblingIndex를 반환합니다.")
    public ResponseEntity<?> checkWriting(
            @RequestParam(name = "parentDepth") int parentDepth,
            @RequestParam(name = "parentSiblingIndex") int parentSiblingIndex,
            @RequestParam(name = "title") String title) {
        try {
            int nextSiblingIndex = writingService.checkWriting(parentDepth, parentSiblingIndex, title);
            return ResponseEntity.ok(nextSiblingIndex);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 스냅샷관련
    @PostMapping
    @Operation(summary = "글 저장", description = "WritingDto를 전달받아 글을 저장합니다.")
    public ResponseEntity<?> saveWriting(@RequestBody WritingDto writingDto) {
        try {
            return ResponseEntity.ok(writingService.saveWriting(writingDto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/parents")
    @Operation(summary = "부모 글들 조회", description = "자식 글조각을 선택해서 루트까지 부모 조각을 찾고 순서대로 보여준다."
    		+ "글을 선택한 부분부터 순서대로 보고싶을 때 사용한다."
    		+ "받는 WritingDto는 depth(이어쓰기 번째)와 siblingIndex(같은 depth에서 1번째글인지 2번째글인지)와 title만 담으면된다,"
    		+ "List<WritingDto>를 리턴한다")
    public ResponseEntity<List<WritingDto>> getParents(@RequestBody WritingDto dto) {
        return ResponseEntity.ok(writingService.getWritingWithParents(dto));
    }

    @PostMapping("/simple")
    @Operation(summary = "content의 Writing 전체 간략 조회", description = "해당 content의 모든 Writing을 단순히 조회합니다,"
    		+ " depth, siblingIndex, time, email(유저정보)만 가져오게된다. 버전과 시간 유저정보를 가져오는 셈이다,"
    		+ "title만(제목)이 필요하다., List<WritingDto> 를 리턴한다.")
    public ResponseEntity<List<WritingDto>> getSimple(@RequestParam(name = "title")String title) {
        return ResponseEntity.ok(writingService.getSimpleWriting(title));
    }

    @DeleteMapping("/delete/root")
    @Operation(summary = "사용자 루트 글 삭제", description = "글쓴 본인이 루트 글을 삭제합니다. "
    		+ "실제 삭제는 아니며 본문을 빈 문자열로 처리합니다. 트롤 방지책으로 글을 지워버리고 임시로 루트의 이름을 달아둠,"
    		+ "삭제할 WritingDto를 받음")
    public ResponseEntity<?> deleteRoot(@RequestBody WritingDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인되지 않음");
        }

        String email = (String) auth.getPrincipal();

        try {
            writingService.deleteByRoot(email, dto);
            return ResponseEntity.ok("루트 글 삭제 처리 완료");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/admin")
    @Operation(summary = "관리자 글 삭제", description = "현재 유저가 관리자 유저면 특정 글을 삭제 처리합니다. "
    		+ "실제 삭제는 아니며 본문을 빈 문자열로 처리합니다."
    		+ "글쓴이는 관리자 계정으로 대체됩니다,"
    		+ "삭제할 WritingDto를 받음.")
    public ResponseEntity<?> deleteByAdmin(@RequestBody WritingDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인되지 않음");
        }

        String email = (String) auth.getPrincipal();

        try {
            writingService.deleteByAdmin(email, dto);
            return ResponseEntity.ok("관리자 삭제 완료");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/room/{roomId}")
    @Operation(summary = "문서방 글 조회", description = "문서방(Room ID)에 해당하는 글 트리를 조회하여 반환합니다."
    		+ "문서방의 roomId가 정해지고 구독하고나서 불러오면 된다,"
    		+ "roomId를 받고, List<WritingDto>를 리턴한다.")
    public ResponseEntity<List<WritingDto>> getRoomWritings(@PathVariable(name = "roomId") String roomId) {
        try {
            List<WritingDto> result = writingService.getWritingsWithRoomId(roomId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    
    @GetMapping("/get/offical")
    @Operation(summary = "official로 지정한 글을 가져옵니다.", description = "만약 유저가 채택하고 싶은 글이 있고"
    		+ "official로 채택한 버전이 있으면 루트부터 그 글까지 가져옵니다."
    		+ "지정을 하지 않으면 루트에 해당되는 글만 불러옵니다."
    		+ "contentDto를 요청하고 contentDto를 받으면 바로 호출해야합니다."
    		+ "contentDto를 받고 List<WritingDto>를 리턴합니다.")
    public ResponseEntity<List<WritingDto>> getOffical(@RequestBody ContentDto contentDto){
    	List<WritingDto> writingDtos = writingService.getOfficial(contentDto);
    	return ResponseEntity.ok(writingDtos);
    }
    
    @PostMapping("/set/official")
    @Operation(summary = "official을 새로 지정합니다.", description = "루트 유저가 official로 채택하고 싶은 writingDto가 있다면"
    		+ "골라서 채택합니다."
    		+ "official로 채택할 버전을 지정하면 그 글을 불러옵니다."
    		+ "writingDto(depth, siblingIndex, title있는버전)으로 호출합니다."
    		+ "List<WritingDto>를 리턴합니다.")
    public ResponseEntity<List<WritingDto>> setOffical(@RequestBody WritingDto writingDto){
    	
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        String email = (String) auth.getPrincipal();
    	
    	List<WritingDto> writingDtos = writingService.setOfficial(writingDto, email);
    	
    	// TODO: 여기서 장르 서비스 불러야함.
    	
    	return ResponseEntity.ok(writingDtos);
    }
    

    @PostMapping("/delete/except-official")
    @Operation(summary = "official로 지정한 것 제외 삭제.", description = "루트 유저가 official로 채택한 글 조각을 "
    		+ "제외하고 모두 삭제합니다."
    		+ "contentDto를 받고, 해당 official을 리턴합니다.")
    public ResponseEntity<List<WritingDto>> deleteWithoutOfficial(@RequestBody ContentDto contentDto) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        String email = (String) auth.getPrincipal();
    	
        List<WritingDto> remainings = writingService.deleteWithoutOfficial(contentDto, email);
        return ResponseEntity.ok(remainings);
    }
}