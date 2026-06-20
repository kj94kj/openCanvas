package cauCapstone.openCanvas.rdb.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cauCapstone.openCanvas.rdb.dto.MyLikedCoverResponseDto;
import cauCapstone.openCanvas.rdb.dto.MyWritingCoverResponseDto;
import cauCapstone.openCanvas.rdb.dto.UserResponseDto;
import cauCapstone.openCanvas.rdb.entity.User;
import cauCapstone.openCanvas.rdb.service.UserService;
import cauCapstone.openCanvas.rdb.service.WritingService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
	
    private final UserService userService;
    private final WritingService writingService;

    @PutMapping("/color")
    @Operation(
    	    summary = "사용자 글쓰기 색상 변경",
    	    description = "현재 로그인한 사용자의 글쓰기 색상을 변경합니다. color 값이 필요합니다."
    	)
    public ResponseEntity<String> updateColor(@RequestParam(name = "color") String color) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인되지 않음");
        }

        String email = (String) auth.getPrincipal();
        User updatedUser = userService.saveColor(email, color);

        return ResponseEntity.ok("색상 변경 성공: " + updatedUser.getColor());
    }

    @GetMapping("/likes")
    @Operation(summary = "유저가 좋아요한 coverDto 반환", description = "유저가 좋아요한 coverDto 반환, "
    		+ "좋아요를 누른 작품을 찾을 수 있습니다.")
    public ResponseEntity<List<MyLikedCoverResponseDto>> getLikedContents() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
        	return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = (String) auth.getPrincipal();
        List<MyLikedCoverResponseDto> result = userService.getMyLikedCovers(email);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/")
    @Operation(summary = "현재 유저 정보", description = "현재 로그인한 userDto가 반환됨")
    public ResponseEntity<UserResponseDto> getUserInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        String email = (String) auth.getPrincipal();
        return userService.getUser(email)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }
    
    @GetMapping("/writings")
    @Operation(
    	    summary = "내가 작성한 커버 목록 조회",
    	    description = "현재 로그인한 사용자가 작성한 작품 목록을 조회합니다."
    	)
    public ResponseEntity<List<MyWritingCoverResponseDto>> getMyWritingCovers() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        String email = (String) auth.getPrincipal();

        List<MyWritingCoverResponseDto> result = writingService.getMyWritingCovers(email);

        return ResponseEntity.ok(result);
    }
}
