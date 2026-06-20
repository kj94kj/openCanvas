package cauCapstone.openCanvas.rdb.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import cauCapstone.openCanvas.rdb.dto.CoverDto;
import cauCapstone.openCanvas.rdb.dto.CoverRequestDto;
import cauCapstone.openCanvas.rdb.entity.Content;
import cauCapstone.openCanvas.rdb.entity.Cover;
import cauCapstone.openCanvas.rdb.entity.Role;
import cauCapstone.openCanvas.rdb.entity.RoomType;
import cauCapstone.openCanvas.rdb.entity.User;
import cauCapstone.openCanvas.rdb.repository.ContentRepository;
import cauCapstone.openCanvas.rdb.repository.CoverRepository;
import cauCapstone.openCanvas.rdb.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CoverService {
	private final CoverRepository coverRepository;
	private final UserRepository userRepository;
	private final ContentRepository contentRepository;
	
	// cover, content가 같이 생성됨.
	@Transactional
	public Cover makeCover(CoverRequestDto coverRequestDto) {
		Cover cover = coverRequestDto.toEntity();
	    cover.setTime(LocalDateTime.now());
		cover.setRoomType(RoomType.AVAILABLE);
		cover.setRoomId(UUID.randomUUID().toString());;
		coverRepository.save(cover);
		
		Content content = new Content(cover);
		contentRepository.save(content);
		
		return cover;
	}
	
	public Page<CoverDto> showAllCovers(int page, int size){
		Pageable pageable = PageRequest.of(page, size);
		
		return coverRepository.findAllWithLikeCountByIdDesc(pageable);
	}
	
	public Page<CoverDto> showAllCoversWithLikes(int page, int size){
		Pageable pageable = PageRequest.of(page, size);
		
		return coverRepository.findAllOrderByLikeCountDesc(pageable);
	}
	
	public Page<CoverDto> showAllCoversWithViews(int page, int size){
		Pageable pageable = PageRequest.of(page, size);
		
		return coverRepository.findAllOrderByViewDesc(pageable);
	}
	
    public void deleteCover(Long id, String email) {
        User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    	
        if(user.getRole() == Role.ADMIN) {
            Cover cover = coverRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Cover입니다."));

                coverRepository.delete(cover);
        }else {
        	throw new IllegalArgumentException("유저가 어드민이 아닙니다.");
        }
    }
    
    public List<CoverDto> searchCoversByTitle(String keyword) {
       return coverRepository.searchByTitleKeyword(keyword);
    }
    
    public CoverDto checkCover(Long coverId) {
        return coverRepository.findById(coverId)
            .map(c -> CoverDto.fromEntity(c, null))
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Cover입니다."));
    }
    
    public RoomType getRoomType(Long coverId) {
        return coverRepository.findRoomTypeByCoverId(coverId)
                .orElseThrow(() -> new IllegalArgumentException("해당 cover가 없습니다. coverId = " + coverId));
    }
}
