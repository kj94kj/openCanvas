package cauCapstone.openCanvas.rdb.dto;

import java.time.LocalDateTime;

import cauCapstone.openCanvas.rdb.entity.Cover;
import cauCapstone.openCanvas.rdb.entity.RoomType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoverRequestDto {
	// title, coverImageUrl, time, limit
	
	private String title;
	private String coverImageUrl;
	private Integer limit;
	
	public Cover toEntity() {
		return new Cover(title, coverImageUrl, limit);
	}	// 좋아요 개수
	
}
