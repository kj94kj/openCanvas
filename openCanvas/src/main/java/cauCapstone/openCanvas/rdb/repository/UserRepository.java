package cauCapstone.openCanvas.rdb.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cauCapstone.openCanvas.rdb.dto.MyLikedCoverResponseDto;
import cauCapstone.openCanvas.rdb.entity.Content;
import cauCapstone.openCanvas.rdb.entity.Cover;
import cauCapstone.openCanvas.rdb.entity.LikeType;
import cauCapstone.openCanvas.rdb.entity.Role;
import cauCapstone.openCanvas.rdb.entity.User;

public interface UserRepository extends JpaRepository<User, Long>{
	
	// 좋아요를 누른 커버만 반환
	@Query("""
		    select new cauCapstone.openCanvas.rdb.dto.MyLikedCoverResponseDto(
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
		        cover.time
		    )
		    from Like l
		    join l.content content
		    join content.cover cover
		    where l.user.id = :userId
		          and l.liketype = :likeType
		""")
		List<MyLikedCoverResponseDto> findLikedCoversByUserId(@Param("userId") Long userId,@Param("likeType") LikeType likeType);
	
	// TODO: 유저의 색을 정하는 메소드 구현하기(그냥 색 세팅만 해주면됨).
	Optional<User> findByEmail(String email);
	
	List<User> findAllByRole(Role role);
	
}
