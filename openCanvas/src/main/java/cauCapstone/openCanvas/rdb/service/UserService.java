package cauCapstone.openCanvas.rdb.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import cauCapstone.openCanvas.rdb.dto.MyLikedCoverResponseDto;
import cauCapstone.openCanvas.rdb.dto.UserResponseDto;
import cauCapstone.openCanvas.rdb.entity.LikeType;
import cauCapstone.openCanvas.rdb.entity.User;
import cauCapstone.openCanvas.rdb.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	
    public User saveColor(String email, String color) {	
        User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    	
        user.setColor(color);
        
    	return userRepository.save(user); 
    }
	
    public Optional<UserResponseDto> getUser(String email) { 
    	
        return userRepository.findByEmail(email)
                .map((user) -> UserResponseDto.fromEntity(user));
    }
	
    public List<MyLikedCoverResponseDto> getMyLikedCovers(String email){
        User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    	
    	return userRepository.findLikedCoversByUserId(user.getId(), LikeType.LIKE);
    }
}
