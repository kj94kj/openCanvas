package cauCapstone.openCanvas.rdb.dto;

import java.time.LocalDateTime;

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
@Schema(description = "내가 쓴 글의 커버 목록 응답 DTO")
public class MyWritingCoverResponseDto {
    @Schema(description = "커버 ID")
    private Long coverId;
    
    @Schema(description = "content ID")
    private Long contentId;
    
    @Schema(description = "글 제목")
    private String title;
    
    @Schema(description = "커버 이미지 URL")
    private String coverImageUrl;
    
    @Schema(description = """
            문서방 상태.
            EDITING: 현재 편집 중.
            AVAILABLE: 이어쓰기 가능.
            COMPLETE: 완성됨.
            """)
    private RoomType roomType;
    
    @Schema(description = "조회수")
    private int view;
    
    @Schema(description = "좋아요 개수")
    private Long likeCount;
    
    @Schema(description = "커버 생성 시간")
    private LocalDateTime coverTime;
    
    @Schema(description = "내가 쓴 글의 개수")
    private Long myWritingCount;

}
