package cauCapstone.openCanvas.rdb.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import cauCapstone.openCanvas.rdb.dto.ContentDto;
import cauCapstone.openCanvas.rdb.dto.FinalContentDto;
import cauCapstone.openCanvas.rdb.dto.FirstContentDto;
import cauCapstone.openCanvas.rdb.dto.ResCommentDto;
import cauCapstone.openCanvas.rdb.dto.SimpleWritingDto;
import cauCapstone.openCanvas.rdb.dto.WritingDto;
import cauCapstone.openCanvas.rdb.entity.CommentLike;
import cauCapstone.openCanvas.rdb.entity.Content;
import cauCapstone.openCanvas.rdb.entity.Cover;
import cauCapstone.openCanvas.rdb.entity.Like;
import cauCapstone.openCanvas.rdb.entity.LikeType;
import cauCapstone.openCanvas.rdb.entity.User;
import cauCapstone.openCanvas.rdb.repository.CommentLikeRepository;
import cauCapstone.openCanvas.rdb.repository.ContentRepository;
import cauCapstone.openCanvas.rdb.repository.CoverRepository;
import cauCapstone.openCanvas.rdb.repository.LikeRepository;
import cauCapstone.openCanvas.rdb.repository.UserRepository;
import cauCapstone.openCanvas.rdb.repository.WritingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContentService {
	private final ContentRepository contentRepository;
	private final CoverRepository coverRepository;
	private final LikeRepository likeRepository;
	private final UserRepository userRepository;
	private final CommentLikeRepository commentLikeRepository;
	private final WritingRepository writingRepository;
	
	// Content가 아직 생성되지 않았다면 Cover를 기반으로 생성한다.
	public ContentDto getContent(Long coverId, String email, boolean isView) {
        User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

	    Content content = contentRepository.findByCoverId(coverId)
	        .orElseGet(() -> {
	        	
	            Cover cover = coverRepository.findById(coverId)
	                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Cover입니다."));

	            Content newContent = new Content(cover);
	            return contentRepository.save(newContent);
	        });
	    
	    Content conWithComments = contentRepository.findByIdWithComments(content.getId()); 
	    
	    if(isView) {
		    conWithComments.setView(conWithComments.getView() + 1);
		    contentRepository.save(conWithComments);	
	    }
	    
	    int likeNum = contentRepository.countLikesById(conWithComments.getId());
	    
	    Optional<Like> like = likeRepository.findByUserIdAndContentId(user.getId(), conWithComments.getId());
	    LikeType likeType = like.map((a) -> a.getLiketype()).orElse(null);
	    
	    
	    List<ResCommentDto> commentDtos = conWithComments.getComments().stream()
	    	    .map(comment -> {
	    	        Long commentId = comment.getId();

	    	        int comLikeNum = commentLikeRepository.countByCommentIdAndLikeType(commentId, LikeType.LIKE);
	    	        int comDisLikeNum = commentLikeRepository.countByCommentIdAndLikeType(commentId, LikeType.DISLIKE);

	    	        LikeType myType = commentLikeRepository.findByUserIdAndCommentId(user.getId(), commentId)
	    	            .map(CommentLike::getLikeType)
	    	            .orElse(null);
	    	        
	    	        return ResCommentDto.fromEntity(comment, comLikeNum, comDisLikeNum, myType);

	    	    })
	    	    .toList();

	    return ContentDto.fromEntityWithLike(commentDtos, conWithComments, likeNum, likeType);
	    
	}
	
    @Transactional
    public boolean toggleLike(String email, Long coverId, LikeType newLikeType) {
        User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        
        Content oldContent = contentRepository.findByCoverId(coverId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 컨텐츠입니다."));
        
        Long contentId = oldContent.getId();
    	
        Optional<Like> existingLikeOpt = likeRepository.findByUserIdAndContentId(user.getId(), contentId);

        // 사용자가 좋아요 또는 싫어요를 이미 눌렀을 때
        if (existingLikeOpt.isPresent()) {
            Like existingLike = existingLikeOpt.get();

            // 같은 타입을 또 누른 경우 → 삭제 (토글 취소)
            if (existingLike.getLiketype() == newLikeType) {
                likeRepository.delete(existingLike);
                
                return false;
            }

            // 다른 타입을 누른 경우 → 기존 삭제 후 새로 생성
            likeRepository.delete(existingLike);

        }

        Like newLike = new Like();
        newLike.setUser(userRepository.getReferenceById(user.getId()));
        newLike.setContent(contentRepository.getReferenceById(contentId));
        newLike.setLiketype(newLikeType);

        likeRepository.save(newLike);
        
        return true;
    }
    
    @Transactional
    public boolean likeCheck(String email, Long coverId) {
        User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        
        Content oldContent = contentRepository.findByCoverId(coverId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 컨텐츠입니다."));
        
        Long contentId = oldContent.getId();
    	
        Optional<Like> existingLikeOpt = likeRepository.findByUserIdAndContentId(user.getId(), contentId);
        
        if(existingLikeOpt.isPresent()) {
        	return true;
        }else {
        	return false;
        }
    }
    
    public void recommendSet(List<WritingDto> writingDto) {
    	
    }

    public FinalContentDto getFinalContentByCoverId(Long coverId) {
    	
	    Content content = contentRepository.findByCoverId(coverId)
	            .orElseThrow(() -> new IllegalArgumentException(
	            		"해당 coverId의 content가 없습니다. coverId = " + coverId
	            ));
	    
	    content.setView(content.getView() + 1);
	    contentRepository.save(content);	
	    
        FirstContentDto firstContentDto =
                contentRepository.findFirstContentDtoByCoverId(coverId);

        if (firstContentDto == null) {
            throw new IllegalArgumentException("해당 coverId의 content가 없습니다. coverId = " + coverId);
        }

        List<SimpleWritingDto> simpleWritingDtos =
                writingRepository.findSimpleWritingDtosByContentId(firstContentDto.getId());

        return new FinalContentDto(firstContentDto, simpleWritingDtos);
    }
}
