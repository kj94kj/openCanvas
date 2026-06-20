package cauCapstone.openCanvas.rdb.dto;

import cauCapstone.openCanvas.rdb.entity.RoomType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "content에 관한 dto")
public class FirstContentDto {
    @Schema(description = "글 ID")
	private Long id;
    
    @Schema(description = "조회수")
	private int view;
    
    @Schema(description = "좋아요 개수")
	private Integer likeCount;
    
    @Schema(description = "커버 ID")
	private Long coverId;
    
    @Schema(description = "글 제목")
	private String title;
    
    @Schema(description = """
            문서방 상태.
            EDITING: 현재 편집 중이며 roomId가 함께 제공됨.
            AVAILABLE: 이어쓰기 가능.
            COMPLETE: 완성됨.
            """)
	private RoomType roomType;
    
    @Schema(description = "문서방 ID.")
	private String roomId;
    
    @Schema(description = "최대 이어쓰기 가능 작가 수")
	private int limit;
}
