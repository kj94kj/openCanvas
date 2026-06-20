package cauCapstone.openCanvas.rdb.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cauCapstone.openCanvas.rdb.entity.CommentLike;
import cauCapstone.openCanvas.rdb.entity.LikeType;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long>{
	
    Optional<CommentLike> findByUserIdAndCommentId(Long userId, Long commentId);
    
    void deleteByUserIdAndCommentId(Long userId, Long commentId);
    
    long countByCommentId(Long commentId);
    
    int countByCommentIdAndLikeType(Long commentId, LikeType likeType);

}
