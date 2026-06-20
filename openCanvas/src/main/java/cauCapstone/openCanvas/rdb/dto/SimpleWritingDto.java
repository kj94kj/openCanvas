package cauCapstone.openCanvas.rdb.dto;

import java.time.LocalDateTime;

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
		동일한 contentId에 속한 모든 글을 간단하게(글은 적은 양만) 불러온다.
		""")
public class SimpleWritingDto {
	
    @Schema(description = "글 조각 ID")
	private Long id;
    
    @Schema(description = "글 조각의 깊이. 첫 번째 글은 깊이가 1이다.")
	private int depth;
    
    @Schema(description = "같은 depth 안에서의 글 조각 순서")
	private int siblingIndex;
    
    @Schema(description = "부모 글 조각의 siblingIndex")
	private int parentSiblingIndex;
    
    @Schema(description = "글 조각의 일부 내용 50자 정도까지만")
    private String body;

    @Schema(description = "글 조각 작성 시간")
    private LocalDateTime time;

    @Schema(description = "작성자 ID")
    private Long userId;

    @Schema(description = "작성자 이름")
    private String username;

    @Schema(description = "이 글 조각이 속한 content ID")
    private Long contentId;
}
