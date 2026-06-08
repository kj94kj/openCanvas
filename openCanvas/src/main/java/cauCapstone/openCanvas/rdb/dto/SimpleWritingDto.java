package cauCapstone.openCanvas.rdb.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// content 처음 조회했을 때 쓰는 용도임.
// content에 쓰는 dto와 합쳐서 하나의 dto로 만들어서 리턴할예정.

// content와 합치지 않고 그냥 사용할때도 씀.
@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SimpleWritingDto {
	
	private Long id;
	private int depth;
	private int siblingIndex;
	private int parentSiblingIndex;
	private String body;
	private LocalDateTime time;
	private Long userId;
	private String username;
	private Long contentId;
}
