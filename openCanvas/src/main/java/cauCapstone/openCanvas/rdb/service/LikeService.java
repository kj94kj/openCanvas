package cauCapstone.openCanvas.rdb.service;

import java.util.List;

import org.springframework.stereotype.Service;

import cauCapstone.openCanvas.rdb.dto.CoverDto;
import cauCapstone.openCanvas.rdb.entity.Like;
import cauCapstone.openCanvas.rdb.entity.LikeType;
import cauCapstone.openCanvas.rdb.entity.User;
import cauCapstone.openCanvas.rdb.repository.LikeRepository;
import cauCapstone.openCanvas.rdb.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LikeService {
	private final LikeRepository likeRepository;
	private final UserRepository userRepository;
	
	public List<CoverDto> getLikeCover(String email){
        User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        
		List<Like> likes = likeRepository.findByUserIdAndLiketype(user.getId(), LikeType.LIKE);
		
		if(likes.isEmpty()) {
			return List.of();
		}
		
		List<CoverDto> coverDtos = likes.stream()
				.map(like -> CoverDto.fromEntity(like.getContent().getCover(), like.getContent().getId()))
				.distinct()
				.toList();
		
		return coverDtos;
	}
}
