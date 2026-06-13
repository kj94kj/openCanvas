package cauCapstone.openCanvas.rdb.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import cauCapstone.openCanvas.rdb.dto.ContentDto;
import cauCapstone.openCanvas.rdb.dto.FinalContentDto;
import cauCapstone.openCanvas.rdb.entity.LikeType;
import cauCapstone.openCanvas.rdb.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/content")
public class ContentController {

    private final ContentService contentService;

    /*
    @GetMapping("/{coverId}")
    @Operation(summary = "컨텐츠 조회", description = "커버 ID에 해당하는 컨텐츠를 조회하거나 없으면 생성하며, "
    		+ "조회수 증가 및 내가 좋아요 또는 싫어요를 눌렀는지도 알려줌,"
    		+ "coverId가 필요하고, 성공했을때는 contentDto를 실패했을때는 string을 리턴함")
    public ResponseEntity<?> getContent(@PathVariable(name = "coverId") Long coverId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인되지 않음");
        }
        String email = (String) auth.getPrincipal();
        ContentDto contentDto = contentService.getContent(coverId, email, true);
        return ResponseEntity.ok(contentDto);
    }
    */

    @PostMapping("/like-toggle")
    public ResponseEntity<?> toggleLike(
            @RequestParam(name = "coverId") Long coverId,
            @RequestParam(name = "likeType") LikeType likeType) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인되지 않음");
        }
        String email = (String) auth.getPrincipal();
        return ResponseEntity.ok(contentService.toggleLike(email, coverId, likeType));
    }
    
    // 처음에 유저가 좋아요 눌렀는지 확인용
    @GetMapping("/like-check")
    public ResponseEntity<?> likeCheck(
            @RequestParam(name = "coverId") Long coverId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인되지 않음");
        }
        String email = (String) auth.getPrincipal();
        return ResponseEntity.ok(contentService.likeCheck(email, coverId));
    }
    
    @GetMapping("/{coverId}")
    @Operation(summary = "컨텐츠 조회")
    public ResponseEntity<FinalContentDto> getContent(@PathVariable("coverId") Long coverId) {
        return ResponseEntity.ok(contentService.getFinalContentByCoverId(coverId));
    }
}