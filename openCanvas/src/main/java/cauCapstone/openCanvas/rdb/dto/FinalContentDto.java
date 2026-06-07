package cauCapstone.openCanvas.rdb.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//FirstContentDto와 SimpleWritingDto를 합쳐서 프론트에 보내야함.
@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FinalContentDto {
	
	private FirstContentDto firstContentDto;
	private List<SimpleWritingDto> simpleWritingDtos = new ArrayList<SimpleWritingDto>();
}
