package cauCapstone.openCanvas.rdb.dto;

import java.util.ArrayList;
import java.util.List;

import cauCapstone.openCanvas.rdb.entity.Like;
import cauCapstone.openCanvas.rdb.entity.Role;
import cauCapstone.openCanvas.rdb.entity.User;
import cauCapstone.openCanvas.rdb.entity.Writing;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = """
		사용자 정보 응답 DTO.
		현재 요청 시에는 토큰으로 사용자를 식별하므로 일반 요청 DTO로는 사용하지 않는다.
		""")
public class UserDto {
	
    @Schema(description = "사용자 ID")
	private Long id;
    
    @Schema(description = "사용자 닉네임. 현재는 이메일이다.")
	private String nickname;
    
	@Schema (description = "이메일")
	private String email;
	
	@Schema (description = "글씨 색상")
	private String color;
	
    @Schema(description = "사용자 역할. 일반 사용자는 USER, 관리자는 ADMIN")
	private Role role;
    
    @Schema(description = "사용자가 누른 좋아요/싫어요 목록. 필요한 API에서만 사용")
	private List<LikeDto> likeDtos = new ArrayList<>();
    
    @Schema(description = "사용자가 작성한 글 조각 목록. 필요한 API에서만 사용")
	private List<WritingDto> writingDtos = new ArrayList<>();

	public UserDto(String nickname, String email, Role role) {
		this.nickname = nickname;
		this.email = email;
		this.role = role;
	}
	
	public UserDto(String nickname, String email, String color, Role role) {
		this.nickname = nickname;
		this.email = email;
		this.color = color;
		this.role = role;
	}
	
	public User toEntity() {
		return new User(nickname, email, role);
	}
	
	// 유저의 색상을 정해야할때 
    public User toEntityColor() {
        return new User(nickname, email, color, role);
    }
}
