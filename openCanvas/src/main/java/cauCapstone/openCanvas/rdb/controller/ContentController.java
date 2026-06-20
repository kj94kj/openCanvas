package cauCapstone.openCanvas.rdb.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/like-toggle")
    @Operation(
        summary = "컨텐츠 좋아요/싫어요 토글",
        description = """
            로그인한 사용자가 특정 컨텐츠에 좋아요 또는 싫어요를 토글합니다.
            coverId와 likeType이 필요합니다.
            likeType 값은 LIKE 또는 DISLIKE입니다.
            이미 같은 반응을 누른 경우 취소되고, 반대 반응을 누른 경우 변경됩니다.
            """
    )
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
    
    @GetMapping("/like-check")
    @Operation(
    	    summary = "컨텐츠 좋아요/싫어요 여부 확인",
    	    description = """
    	        로그인한 사용자가 특정 컨텐츠에 좋아요 또는 싫어요를 눌렀는지 확인합니다.
    	        coverId가 필요합니다.
    	        """
    	)
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
    @Operation(
    	    summary = "컨텐츠 조회",
    	    description = """
    	        coverId에 해당하는 컨텐츠를 조회합니다.
    	        조회 결과는 FinalContentDto로 반환됩니다.
    	        """
    	)
    public ResponseEntity<FinalContentDto> getContent(@PathVariable("coverId") Long coverId) {
        return ResponseEntity.ok(contentService.getFinalContentByCoverId(coverId));
    }
}