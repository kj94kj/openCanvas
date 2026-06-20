package cauCapstone.openCanvas.rdb.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class FirstController {
	
	// 아무동작 안하게 해도됨.
    @GetMapping("/")
    @Operation(summary = "서버 상태 확인", description = "단순히 서버 실행 상태를 확인합니다.")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("openCanvas server is running");
    }
    
}
