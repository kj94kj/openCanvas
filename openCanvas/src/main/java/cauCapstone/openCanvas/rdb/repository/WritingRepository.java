package cauCapstone.openCanvas.rdb.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cauCapstone.openCanvas.rdb.dto.MyWritingCoverResponseDto;
import cauCapstone.openCanvas.rdb.dto.SimpleWritingDto;
import cauCapstone.openCanvas.rdb.dto.WritingDto;
import cauCapstone.openCanvas.rdb.entity.Writing;

public interface WritingRepository extends JpaRepository<Writing, Long> {
	
    // 특정 depth에서 해당 siblingIndex를 사용할 수 있는지 확인한다.
	@Query("SELECT COUNT(w) FROM Writing w WHERE w.depth = :depth AND w.siblingIndex = :siblingIndex "
			+ "AND w.content.title = :title")
	int countByDepthAndSiblingIndex(@Param("depth") int depth, @Param("siblingIndex") int siblingIndex, @Param("title") String title);
    
	@Query("""
		    SELECT w FROM Writing w
		    WHERE w.user.email = :email
		""")
		List<Writing> findByUserName(@Param("email") String email);
	
	@Query("""
			SELECT w FROM Writing w
			WHERE w.user.email = :email AND w.content.title = :title
			""")
	Optional<Writing> findByUserNameAndTitle(@Param("email") String email, @Param("title") String title);
	
	Optional<Writing> findByDepthAndSiblingIndexAndContent_Title(int depth, int siblingIndex, String title);
	
	@Query("""
		    SELECT new cauCapstone.openCanvas.rdb.dto.WritingDto(
		        w.depth, w.siblingIndex, w.time, u.email)
		    FROM Writing w
		    JOIN w.user u
		    JOIN w.content c
		    LEFT JOIN w.parent p
		    WHERE w.content.title = :title
		    ORDER BY w.depth ASC, w.siblingIndex ASC
		""")
		List<WritingDto> findAllDtosByContentTitle(@Param("title") String title);
	
    // 부모가 없는 루트 Writing은 parentIndex를 0으로 해준다.
	@Query("""
		    SELECT new cauCapstone.openCanvas.rdb.dto.SimpleWritingDto(
			    w.id,
			    w.depth,
			    w.siblingIndex,
			    coalesce(p.siblingIndex, 0),
			    substring(coalesce(w.body, ''), 1, 80),
			    w.time,
			    u.id,
			    u.nickname,
			    w.content.id
		    )
		    FROM Writing w
		    JOIN w.user u
		    LEFT JOIN w.parent p
		    WHERE w.content.id = :contentId
		    ORDER BY w.depth ASC, w.siblingIndex ASC
		""")
		List<SimpleWritingDto> findSimpleWritingDtosByContentId(@Param("contentId") Long contentId);
	
	
	List<Writing> findAllByParent(Writing parent);
	
	List<Writing> findAllByContent_Title(String title);
	
	@Query("""
		    select new cauCapstone.openCanvas.rdb.dto.MyWritingCoverResponseDto(
		        cover.id,
		        content.id,
		        cover.title,
		        cover.coverImageUrl,
		        cover.roomType,
		        content.view,
		        (
		            select count(l.id)
		            from Like l
		            where l.content.id = content.id
		        ),
		        cover.time,
		        count(writing.id)
		    )
		    from Writing writing
		    join writing.content content
		    join content.cover cover
		    where writing.user.id = :userId
		    group by cover.id, content.id, cover.title, cover.coverImageUrl, content.view, cover.time
		""")
		List<MyWritingCoverResponseDto> findMyWritingCovers(@Param("userId") Long userId);
}
