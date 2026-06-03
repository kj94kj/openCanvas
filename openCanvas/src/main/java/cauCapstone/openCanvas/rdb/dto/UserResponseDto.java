package cauCapstone.openCanvas.rdb.dto;

import java.util.List;

import cauCapstone.openCanvas.rdb.entity.Role;
import cauCapstone.openCanvas.rdb.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
	
	private Long id;
	private String nickname;
	private String email;
	private Role role;
	
    // UserDto 응답
    public static UserResponseDto fromEntity(User user) {
    	
    	return new UserResponseDto(user.getId(), user.getNickname(), user.getEmail(), user.getRole());
    }
}
