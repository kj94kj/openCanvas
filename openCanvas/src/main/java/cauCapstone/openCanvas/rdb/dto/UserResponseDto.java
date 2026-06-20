package cauCapstone.openCanvas.rdb.dto;

import cauCapstone.openCanvas.rdb.entity.Role;
import cauCapstone.openCanvas.rdb.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 정보 응답 DTO")
public class UserResponseDto {
    
    @Schema(description = "사용자 ID")
    private Long id;

    @Schema(description = "사용자 닉네임")
    private String nickname;

    @Schema(description = "사용자 이메일")
    private String email;

    @Schema(description = "사용자 역할. 일반 사용자는 USER, 관리자는 ADMIN")
    private Role role;
    
    public static UserResponseDto fromEntity(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getNickname(),
                user.getEmail(),
                user.getRole()
        );
    }
}
