package cauCapstone.openCanvas.rdb.dto;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//FirstContentDto와 SimpleWritingDto를 합쳐서 프론트에 보내야함.
@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = """
		최종적인 content 응답용 dto.
		content에 관한 내용과 간단한 글의 정보를 포함함.
		""")
public class FinalContentDto {
	
    @Schema(description = "content 정보")
	private FirstContentDto firstContentDto;
    @Schema(description = "간단한 글 정보")
	private List<SimpleWritingDto> simpleWritingDtos = new ArrayList<SimpleWritingDto>();
}
