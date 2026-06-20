package cauCapstone.openCanvas.rdb.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cauCapstone.openCanvas.rdb.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long>{
	
    // 댓글 조회 시 좋아요 목록까지 함께 조회한다.
	@Query("SELECT c FROM Comment c LEFT JOIN FETCH c.commentLikes WHERE c.id = :id")
	Optional<Comment> findByIdWithCommentLikes(@Param("id") Long id);
	
	Optional<Comment> findByIdAndUserIdAndContentId(Long id, Long userId, Long contentId);
	
	List<Comment> findByContentId(Long contetnId);
	
}
