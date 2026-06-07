package cauCapstone.openCanvas.rdb.dto;

import java.util.List;

import cauCapstone.openCanvas.rdb.entity.RoomType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FirstContentDto {
	private Long id;
	private int view;
	private Integer likeCount;
	private Long coverId;
	private String title;
	private RoomType roomType;
	private String roomId;
	private int limit;
}
