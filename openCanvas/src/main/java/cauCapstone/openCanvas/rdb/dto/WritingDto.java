package cauCapstone.openCanvas.rdb.dto;

import java.time.LocalDateTime;

import cauCapstone.openCanvas.rdb.entity.Content;
import cauCapstone.openCanvas.rdb.entity.User;
import cauCapstone.openCanvas.rdb.entity.Writing;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = """
		글 조각 DTO.
		depth, siblingIndex, parentSiblingIndex로 트리형 이어쓰기 위치를 표현한다.
		요청/응답에서 함께 사용될 수 있다.
		""")
public class WritingDto {
	
	@Schema(description = "글 조각의 깊이. 첫 번째 글 조각은 depth 1")
	private int depth;
	
	@Schema(description = "같은 depth 안에서의 글 조각 순서")
	private int siblingIndex;
	
	@Schema(description = "부모 글 조각의 siblingIndex. 첫 번째 글 조각이면 null")
	private Integer parentSiblingIndex;
	
	@Schema(description = "글 조각 내용")
	private String body;
	
	@Schema(description = "글 조각 작성 시간")
	private LocalDateTime time;
	
	@Schema (description = "작성자 닉네임(현재는 이메일)")
	private String username;
	@Schema (description = "전체 글의 제목")
	private String title;	
	
	@Schema(description = "작성자 ID")
	private Long userId;
	
	@Schema(description = "contentId")
	private Long contentId;	
	
	@Schema(description = "작성자 글씨 색상")
	private String color;
	
	public WritingDto(int depth, int siblingIndex, Integer parentSiblingIndex, String body, LocalDateTime time, 
			String username, String title) {
		this.depth = depth;
		this.siblingIndex = siblingIndex;
		this.parentSiblingIndex = parentSiblingIndex;
		this.body = body;
		this.time = time;
		this.username = username;
		this.title = title;
	}
	
	public WritingDto(int depth, int siblingIndex, Integer parentSiblingIndex, String body, LocalDateTime time, 
			String username, String title, String color) {
		this.depth = depth;
		this.siblingIndex = siblingIndex;
		this.parentSiblingIndex = parentSiblingIndex;
		this.body = body;
		this.time = time;
		this.username = username;
		this.title = title;
		this.color = color;
	}
	
	public WritingDto(int depth, int siblingIndex, LocalDateTime time, String username) {
		this.depth = depth;
		this.siblingIndex = siblingIndex;
		this.time = time;
		this.username = username;
	}
	
	public WritingDto(int depth, int siblingIndex, String title) {
		this.depth = depth;
		this.siblingIndex = siblingIndex;
		this.title = title;
	}
	
	public WritingDto(String title) {
		this.title = title;
		
	}
	
	public static WritingDto fromEntity(Writing writing, String title) {
	    Integer parentSiblingIndex = null;

	    if (writing.getDepth() > 1) {  // depth가 1이면 루트니까 부모 불필요
	        Writing parent = writing.getParent();
	        if (parent != null) {
	            parentSiblingIndex = parent.getSiblingIndex();
	        }
	    }

	    String username = writing.getUser().getNickname();
	    
	    String color = writing.getUser().getColor();
	
		return new WritingDto(writing.getDepth(), writing.getSiblingIndex(), parentSiblingIndex, writing.getBody(), 
				writing.getTime(), username, title, color);
	}
	
	public Writing toEntity(User user, Content content, Writing parent) {
		return new Writing(depth, siblingIndex, parent, body, time, content, user);
	}
}
