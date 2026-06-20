package cauCapstone.openCanvas.rdb.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cauCapstone.openCanvas.rdb.dto.FirstContentDto;
import cauCapstone.openCanvas.rdb.entity.Content;

public interface ContentRepository extends JpaRepository<Content, Long>{
	
	Optional<Content> findByCoverId(Long coverId);
	
	@Query("SELECT c FROM Content c LEFT JOIN FETCH c.comments WHERE c.id = :id")
	Content findByIdWithComments(@Param("id") Long id);
	
    @Query("SELECT COUNT(l) FROM Like l WHERE l.content.id = :contentId AND l.liketype = 'LIKE'")
    int countLikesById(@Param("contentId") Long id);
    
    Optional<Content> findByTitle(String title);
    
    @Query("""
            SELECT new cauCapstone.openCanvas.rdb.dto.FirstContentDto(
                c.id,
                c.view,
                SIZE(c.likes),
                cv.id,
                cv.title,
                cv.roomType,
                cv.roomId,
                cv.limit
            )
            FROM Content c
            JOIN c.cover cv
            WHERE cv.id = :coverId
        """)
        FirstContentDto findFirstContentDtoByCoverId(@Param("coverId") Long coverId);
}
