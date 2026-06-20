package cauCapstone.openCanvas.rdb.dto;

import cauCapstone.openCanvas.rdb.entity.Cover;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "커버 생성 요청 DTO")
public class CoverRequestDto {
	
    @Schema(description = "글 제목")
	private String title;
    @Schema(description = "표지 이미지 URL")
	private String coverImageUrl;
    @Schema(description = "최대 이어쓰기 가능 작가 수")
	private Integer limit;
	
	public Cover toEntity() {
		return new Cover(title, coverImageUrl, limit);
	}	
	
}
