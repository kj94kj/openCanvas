package cauCapstone.openCanvas.rdb.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cauCapstone.openCanvas.rdb.dto.ReqCommentDto;
import cauCapstone.openCanvas.rdb.dto.ResCommentDto;
import cauCapstone.openCanvas.rdb.entity.LikeType;
import cauCapstone.openCanvas.rdb.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/write")
    @Operation(summary = "댓글 작성", description = "로그인한 사용자가 댓글을 작성합니다,"
    		+ "ReqCommentDto(요청용 CommentDto)를 줘야함,"
    		+ "LIst<ResCommentDto>를 리턴함")
    public ResponseEntity<?> writeComment(@RequestBody ReqCommentDto commentDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인되지 않음");
        }
        String email = (String) auth.getPrincipal();
        return ResponseEntity.ok(commentService.save(commentDto, email));
    }

    @GetMapping("/by-content")
    @Operation(summary = "특정 글(content)의 댓글 조회", description = "글에 달린 댓글을 보여주기 위해 쓴다, contentId 필요함,"
    		+ "LIst<ResCommentDto>를 리턴함")
    public ResponseEntity<?> getComments(@RequestParam(name = "contentId") Long contentId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인되지 않음");
        }
        String email = (String) auth.getPrincipal();
    	
        return ResponseEntity.ok(commentService.getContentComments(email, contentId));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "댓글 삭제", description = "유저 본인이 쓴 댓글을 삭제한다, contentId, commentId가 필요하다,"
    		+ "LIst<ResCommentDto>를 리턴함")
    public ResponseEntity<?> deleteComment(
            @RequestParam(name = "commentId") Long commentId,
            @RequestParam(name = "contentId") Long contentId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인되지 않음");
        }
        String email = (String) auth.getPrincipal();
        return ResponseEntity.ok(commentService.deleteComment(commentId, email, contentId));
    }
    
    @PostMapping("/like-toggle")
    @Operation(
        summary = "댓글 좋아요/싫어요 토글",
        description = "사용자가 댓글에 대해 좋아요 또는 싫어요를 토글합니다. commentId(댓글아이디), likeType(유저가 좋아요를 눌렀는지,"
        		+ "싫어요를 눌렀는지 알고 있어야한다)이 필요하다."
        		+ "LIst<ResCommentDto>를 리턴함"
    )
    public ResponseEntity<?> toggleCommentLike(
            @RequestParam(name = "commentId") Long commentId,
            @RequestParam(name = "likeType") LikeType likeType) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인되지 않음");
        }

        String email = (String) auth.getPrincipal();
        
        return ResponseEntity.ok(commentService.toggleLike(email, commentId, likeType));
    }
} 
