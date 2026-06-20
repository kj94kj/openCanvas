package cauCapstone.openCanvas.rdb.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import cauCapstone.openCanvas.rdb.dto.CoverDto;
import cauCapstone.openCanvas.rdb.dto.CoverRequestDto;
import cauCapstone.openCanvas.rdb.entity.RoomType;
import cauCapstone.openCanvas.rdb.service.CoverService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/covers")
public class CoverController {

    private final CoverService coverService;

    @PostMapping
    @Operation(
    	    summary = "커버 생성",
    	    description = """
    	        새로운 커버를 생성합니다.
    	        커버는 캔버스에 입장하기 전에 보이는 표지 정보입니다.
    	        요청 본문으로 CoverRequestDto를 전달해야 하며, 생성된 CoverDto를 반환합니다.
    	        """
    	)
    public ResponseEntity<CoverDto> createCover(@RequestBody CoverRequestDto coverRequestDto) {
        return ResponseEntity.ok(CoverDto.fromEntity(coverService.makeCover(coverRequestDto), null));
    }

    @GetMapping("/new")
    @Operation(
    	    summary = "전체 커버 조회 - 최신순",
    	    description = """
    	        모든 커버를 최신순으로 페이지 조회합니다.
    	        page와 size를 요청 파라미터로 전달할 수 있습니다.
    	        Page<CoverDto>를 반환합니다.
    	        """
    	)
    public ResponseEntity<Page<CoverDto>> getAllCovers(@RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(coverService.showAllCovers(page, size));
    }

    @GetMapping("/likes")
    @Operation(
    	    summary = "전체 커버 조회 - 좋아요순",
    	    description = """
    	        모든 커버를 좋아요 개수 기준으로 정렬하여 페이지 조회합니다.
    	        page와 size를 요청 파라미터로 전달할 수 있습니다.
    	        Page<CoverDto>를 반환합니다.
    	        """
    	)
    public ResponseEntity<Page<CoverDto>> getCoversByLikes(@RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(coverService.showAllCoversWithLikes(page, size));
    }

    @GetMapping("/views")
    @Operation(
    	    summary = "전체 커버 조회 - 조회수순",
    	    description = """
    	        모든 커버를 조회수 기준으로 정렬하여 페이지 조회합니다.
    	        page와 size를 요청 파라미터로 전달할 수 있습니다.
    	        Page<CoverDto>를 반환합니다.
    	        """
    	)
    public ResponseEntity<Page<CoverDto>> getCoversByViews(@RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(coverService.showAllCoversWithViews(page, size));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "커버 삭제", description = "User.ROLE이 ADMIN인 사용자가 커버를 삭제합니다, coverId를 주면 됩니다.")
    public ResponseEntity<String> deleteCover(@PathVariable(name = "id") Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인되지 않음");
        }
        String email = (String) auth.getPrincipal();
        coverService.deleteCover(id, email);
        return ResponseEntity.ok("커버 삭제 성공");
    }
    
    @GetMapping("/search")
    @Operation(summary = "제목 검색", description = "제목에 키워드가 포함된 커버를 검색합니다, 제목(keyword)를 주면 됩니다.")
    public ResponseEntity<List<CoverDto>> searchCovers(@RequestParam(name = "keyword") String keyword) {
        return ResponseEntity.ok(coverService.searchCoversByTitle(keyword));
    }
    
    @GetMapping("/check")
    @Operation(summary = "캔버스 상태 확인", description = "coverId로 캔버스 상태(RoomType 등)를 확인합니다. CoverDto를 반환합니다.")
    public ResponseEntity<CoverDto> checkCoverStatus(@RequestParam(name = "coverId") Long coverId) {
        return ResponseEntity.ok(coverService.checkCover(coverId));
    }
    
    @GetMapping("/{coverId}/room-type")
    @Operation(
    	    summary = "커버의 방 상태 조회",
    	    description = """
    	        coverId에 해당하는 커버의 RoomType 상태를 조회합니다.
    	        RoomType을 반환합니다.
    	        """
    	)
    public ResponseEntity<RoomType> getRoomType(@PathVariable(name = "coverId") Long coverId) {
        return ResponseEntity.ok(coverService.getRoomType(coverId));
    }
}