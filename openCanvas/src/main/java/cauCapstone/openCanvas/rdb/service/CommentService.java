package cauCapstone.openCanvas.rdb.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import cauCapstone.openCanvas.rdb.dto.ReqCommentDto;
import cauCapstone.openCanvas.rdb.dto.ResCommentDto;
import cauCapstone.openCanvas.rdb.entity.Comment;
import cauCapstone.openCanvas.rdb.entity.CommentLike;
import cauCapstone.openCanvas.rdb.entity.Content;
import cauCapstone.openCanvas.rdb.entity.Like;
import cauCapstone.openCanvas.rdb.entity.LikeType;
import cauCapstone.openCanvas.rdb.entity.User;
import cauCapstone.openCanvas.rdb.repository.CommentLikeRepository;
import cauCapstone.openCanvas.rdb.repository.CommentRepository;
import cauCapstone.openCanvas.rdb.repository.ContentRepository;
import cauCapstone.openCanvas.rdb.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {
	private final CommentRepository commentRepository;
	private final ContentRepository contentRepository;
	private final UserRepository userRepository;
	private final CommentLikeRepository commentLikeRepository;
	
	public List<ResCommentDto> save(ReqCommentDto commentDto, String email) {
	    Content content = contentRepository.findById(commentDto.getContentId())
	            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 콘텐츠입니다."));
	        User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

	        Comment comment = commentDto.toEntity(content, user);
	        
	        commentRepository.save(comment);
	        
	        return getContentComments(email, content.getId());
	}
	
	public List<ResCommentDto> getContentComments(String email, Long contentId) {
        User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
		
        Content conWithComments = contentRepository.findByIdWithComments(contentId); 
	  
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
		
	    return commentDtos;
	}
	
	public List<ResCommentDto> deleteComment(Long commentId, String email, Long contentId) {
		User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
		
	    Comment comment = commentRepository.findByIdAndUserIdAndContentId(commentId, user.getId(), contentId)
	            .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않거나 삭제 권한이 없습니다."));

	    commentRepository.delete(comment);
	        
	    return getContentComments(email, contentId);
	}
	
	@Transactional
	public List<ResCommentDto> toggleLike(String email, Long commentId, LikeType newLikeType) {
	    User user = userRepository.findByEmail(email)
	        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

	    Comment comment = commentRepository.findById(commentId)
	        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

	    Optional<CommentLike> existingLikeOpt = commentLikeRepository.findByUserIdAndCommentId(user.getId(), commentId);
	    if (existingLikeOpt.isPresent()) {
	    	CommentLike existingLike = existingLikeOpt.get();
	    	
            if (existingLike.getLikeType() == newLikeType) {
            	commentLikeRepository.delete(existingLike);
                return getContentComments(email, comment.getContent().getId());
            }

            commentLikeRepository.delete(existingLike);
	    }
        CommentLike newLike = new CommentLike();
        newLike.setUser(user);
        newLike.setComment(comment);
        commentLikeRepository.save(newLike);
        
        return getContentComments(email, comment.getContent().getId());
	}
	
	public ResCommentDto getCommentById(Long commentId, String email) {
	    Comment comment = commentRepository.findById(commentId)
	            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

	    User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
	    
	    int likeNum = commentLikeRepository.countByCommentIdAndLikeType(commentId, LikeType.LIKE);
	    int disLikeNum = commentLikeRepository.countByCommentIdAndLikeType(commentId, LikeType.DISLIKE);

	    Optional<CommentLike> likeOpt = commentLikeRepository.findByUserIdAndCommentId(user.getId(), commentId);
	    LikeType myLikeType = likeOpt.map(CommentLike::getLikeType).orElse(null);

	    ResCommentDto dto = ResCommentDto.fromEntity(comment, likeNum, disLikeNum, myLikeType);


	    return dto;
	}
}
