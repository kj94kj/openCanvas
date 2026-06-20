package cauCapstone.openCanvas.rdb.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cauCapstone.openCanvas.rdb.entity.Like;
import cauCapstone.openCanvas.rdb.entity.LikeType;

public interface LikeRepository extends JpaRepository<Like, Long> {
	
	List<Like> findByUserIdAndLiketype(Long userId, LikeType liketype);
	
	Optional<Like> findByUserIdAndContentId(Long userId, Long contentId);
	
	@Query("""
		    SELECT l
		    FROM Like l
		    WHERE l.content.cover.id = :coverId
		      AND l.user.id = :userId
		      AND l.liketype = cauCapstone.openCanvas.rdb.entity.LikeType.LIKE
		""")
		Optional<Like> findLikeByCoverIdAndUserId(
		    @Param("coverId") Long coverId,
		    @Param("userId") Long userId
		);
}
