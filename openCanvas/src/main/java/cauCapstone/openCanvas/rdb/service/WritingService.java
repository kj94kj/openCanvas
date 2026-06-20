package cauCapstone.openCanvas.rdb.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import cauCapstone.openCanvas.rdb.dto.ContentDto;
import cauCapstone.openCanvas.rdb.dto.MyWritingCoverResponseDto;
import cauCapstone.openCanvas.rdb.dto.WritingDto;
import cauCapstone.openCanvas.rdb.entity.Content;
import cauCapstone.openCanvas.rdb.entity.Role;
import cauCapstone.openCanvas.rdb.entity.RoomType;
import cauCapstone.openCanvas.rdb.entity.User;
import cauCapstone.openCanvas.rdb.entity.Writing;
import cauCapstone.openCanvas.rdb.repository.ContentGenreRepository;
import cauCapstone.openCanvas.rdb.repository.ContentRepository;
import cauCapstone.openCanvas.rdb.repository.CoverRepository;
import cauCapstone.openCanvas.rdb.repository.UserRepository;
import cauCapstone.openCanvas.rdb.repository.WritingRepository;
import cauCapstone.openCanvas.websocket.chatroom.ChatRoomRedisEntity;
import cauCapstone.openCanvas.websocket.chatroom.ChatRoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WritingService {

    private final WritingRepository writingRepository;
    private final UserRepository userRepository;
    private final ContentRepository contentRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final CoverRepository coverRepository;
	private final ContentGenreRepository contentGenreRepository;

    // 현재 depth로 글을 써도 되는지 체크함. 이 동작 후에 문서방을 만들면 된다.
    @Transactional
    public int checkWriting(int parentDepth, int parentSiblingIndex, String title) {
        int newDepth = parentDepth + 1;

        boolean is1Used = writingRepository.countByDepthAndSiblingIndex(newDepth, 1, title) > 0;
        boolean is2Used = writingRepository.countByDepthAndSiblingIndex(newDepth, 2, title) > 0;

        if (is1Used && is2Used) {
            throw new IllegalStateException("해당 이어쓰기 단계에서는 이미 2개의 글이 작성되었습니다.");
        }

        // 인덱스 자리는 있는데 1이 사용중이면 2를, 그게 아니라면 1을 할당한다.
        int nextSiblingIndex = is1Used ? 2 : 1;
        
        return nextSiblingIndex;
    }
    
    // Wrting 리프노드에서 부모노드들을 전부 가져오는 역할: 리프노드는 실제 저장이 되있어야한다.
    @Transactional
    public List<WritingDto> getWritingWithParents(WritingDto writingDto) {
    	List<WritingDto> allWritingDtos = new ArrayList<>();
    	
    	int curDepth = writingDto.getDepth();
    	int curSiblingIndex = writingDto.getSiblingIndex();
    	String title = writingDto.getTitle();
    	
    	while(curDepth >0) {
            Writing current = writingRepository
                    .findByDepthAndSiblingIndexAndContent_Title(curDepth, curSiblingIndex, title)
                    .orElseThrow(() ->  new IllegalArgumentException("존재하지 않는 writing입니다."));
                        
                allWritingDtos.add(WritingDto.fromEntity(current, title));
                
                curDepth = curDepth - 1;
                curSiblingIndex = (current.getParent() != null) ? current.getParent().getSiblingIndex() : -1;
    	}
    	
        Collections.reverse(allWritingDtos);
    	
    	return allWritingDtos;
    }
    
    public Writing saveWriting(WritingDto writingDto) {

        User user = userRepository.findByEmail(writingDto.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Content content = contentRepository.findByTitle(writingDto.getTitle())
            .orElseThrow(() -> new IllegalArgumentException("콘텐츠를 찾을 수 없습니다."));

        Writing parent = null;
        if (writingDto.getDepth() > 1) {
            parent = writingRepository
                .findByDepthAndSiblingIndexAndContent_Title(writingDto.getDepth()-1, writingDto.getParentSiblingIndex(), 
                		content.getTitle())
                .orElseThrow(() -> new IllegalArgumentException("부모 글이 존재하지 않습니다."));
        }

        Writing writing = writingDto.toEntity(user, content, parent);
        
        if(writing.getDepth() >= content.getCover().getLimit()) {
        	content.getCover().setRoomType(RoomType.COMPLETE);
        	coverRepository.save(content.getCover());
        	
        }

        return writingRepository.save(writing);
    }
    
    // 글의 최초 작성자가 글을 삭제할 수 있게한다.
    @Transactional
    public void deleteByRoot(String email, WritingDto writingDto) {  	
    	User user = userRepository.findByEmail(email)
    			.orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
    	
    	Writing userWriting = writingRepository.findByUserNameAndTitle(email, writingDto.getTitle())
    	            .orElseThrow(() -> new IllegalArgumentException("유저가 쓴 writing을 찾을 수 없습니다."));
    	
    	if(userWriting.getDepth() == 1) {
            Writing delete = writingRepository
                    .findByDepthAndSiblingIndexAndContent_Title(writingDto.getDepth(), writingDto.getSiblingIndex(), 
                    		writingDto.getTitle())
                    .orElseThrow(() ->  new IllegalArgumentException("존재하지 않는 writing입니다."));
            
            delete.setBody("");
            delete.setUser(user);	// 여기 동작하나 확인
            writingRepository.save(delete);
    	}else {
    		throw new IllegalArgumentException("유저가 루트가 아닙니다.");
    	}
    }
    
    public List<WritingDto> getSimpleWriting(String title){
    	 return writingRepository.findAllDtosByContentTitle(title);
    }
    
    @Transactional
    public void deleteByAdmin(String email, WritingDto writingDto) {
    	User user = userRepository.findByEmail(email)
    			.orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
    	
    	if(user.getRole() == Role.ADMIN) {
            Writing delete = writingRepository
                    .findByDepthAndSiblingIndexAndContent_Title(writingDto.getDepth(), writingDto.getSiblingIndex(), 
                    		writingDto.getTitle())
                    .orElseThrow(() ->  new IllegalArgumentException("존재하지 않는 writing입니다."));
            
            delete.setBody("");
            delete.setUser(user);	// 여기 동작하나 확인
            writingRepository.save(delete);
    	}else {
    		throw new IllegalArgumentException("유저가 어드민이 아닙니다.");
    	}
    }
    
    public List<WritingDto> getWritingsWithRoomId(String roomId){
    	ChatRoomRedisEntity chatRoom = chatRoomRepository.findRoomById(roomId);
    	
    	List<WritingDto> allWritingDtos = new ArrayList<>();
    	
    	List<Integer> version = getIntVersion(chatRoom.getVersion());
    	
    	int curDepth = version.get(0);
    	int curSiblingIndex = version.get(1);
    	String title = chatRoom.getName();
    	
        curDepth = curDepth - 1;
        curSiblingIndex = (version.size() > 2 && version.get(2) != null) ? version.get(2) : -1;
    	
    	while(curDepth >0) {
            Writing current = writingRepository
                    .findByDepthAndSiblingIndexAndContent_Title(curDepth, curSiblingIndex, title)
                    .orElseThrow(() ->  new IllegalArgumentException("존재하지 않는 writing입니다."));
                        
                allWritingDtos.add(WritingDto.fromEntity(current, title));
                
                curDepth = curDepth - 1;
                curSiblingIndex = (current.getParent() != null) ? current.getParent().getSiblingIndex() : -1;
    	}
    	
    	Collections.reverse(allWritingDtos);
    	
    	return allWritingDtos;
    	
    }
    
    public List<Integer> getIntVersion(String version){
        if (version == null || version.isEmpty()) {
            return List.of(); // 또는 예외 던지기
        }
        
        return List.of(version.split("\\.")).stream()
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
    
    // 버전을 String화 한다. 2 1 2 이렇게 리스트에다가 싣으면 2.1.2로 리턴한다.
    public String getStringVersion(List<Integer> versionList) {
        return versionList.stream()
                .map(String::valueOf)
                .collect(Collectors.joining("."));
    }
    
    public List<WritingDto> getOfficial(ContentDto contentDto){
    	String version = contentDto.getOfficial();
    	
    	List<Integer> iversion = getIntVersion(version);
    	
    	WritingDto writingDto = new WritingDto(iversion.get(0), iversion.get(1), contentDto.getTitle());

        try {
            return getWritingWithParents(writingDto);
        } catch (IllegalArgumentException e) {
            // 해당 버전에 글이 하나도 없다는 뜻 → 그냥 빈 리스트 반환
            return List.of();
        }
    }
    
    @Transactional
    public List<WritingDto> setOfficial(WritingDto writingDto, String email) {
        Writing current = writingRepository
                .findByDepthAndSiblingIndexAndContent_Title(writingDto.getDepth(), writingDto.getSiblingIndex(), 
                		writingDto.getTitle())
                .orElseThrow(() ->  new IllegalArgumentException("존재하지 않는 writing입니다."));
        
        Writing root = writingRepository
                .findByDepthAndSiblingIndexAndContent_Title(1, 0, 
                		writingDto.getTitle())
                .orElseThrow(() ->  new IllegalArgumentException("존재하지 않는 writing입니다."));
        
        if (!email.equals(root.getUser().getEmail())) {
            throw new IllegalArgumentException("해당 콘텐츠의 작성자만 official을 설정할 수 있습니다.");
        }
        
        Content content = current.getContent();
        String versionStr = current.getDepth() + "." + current.getSiblingIndex();

        if (current.getParent() != null) {
            versionStr = versionStr + "." + current.getParent().getSiblingIndex();
        }

        content.setOfficial(versionStr);
        
        List<WritingDto> wOfficial = getWritingWithParents(writingDto);
        
        StringBuilder textBuilder = new StringBuilder();
        for (WritingDto dto : wOfficial) {
            textBuilder.append(dto.getBody()).append("\n");
        }
        
		List<String> genreNames = contentGenreRepository.findGenreNamesByContentId(content.getId());

        contentRepository.save(content);

        return wOfficial;
    }
    
    public List<WritingDto> deleteWithoutOfficial(ContentDto contentDto, String email){
    	
        Writing root = writingRepository
                .findByDepthAndSiblingIndexAndContent_Title(1, 0, 
                		contentDto.getTitle())
                .orElseThrow(() ->  new IllegalArgumentException("존재하지 않는 writing입니다."));
    	
        if (!email.equals(root.getUser().getEmail())) {
            throw new IllegalArgumentException("해당 콘텐츠의 작성자만 official을 설정할 수 있습니다.");
        }
        
        String version = contentDto.getOfficial();
        
        List<Integer> iversion = getIntVersion(version);
        
        WritingDto writingDto = new WritingDto(iversion.get(0), iversion.get(1), contentDto.getTitle());
        
        List<WritingDto> officials= getWritingWithParents(writingDto);
        

        List<Writing> allWritings = writingRepository.findAllByContent_Title(contentDto.getTitle());

        List<int[]> officialKeys = officials.stream()
                .map(w -> new int[]{w.getDepth(), w.getSiblingIndex()})
                .toList();

        List<Writing> toDelete = allWritings.stream()
                .filter(w -> officialKeys.stream()
                        .noneMatch(key -> key[0] == w.getDepth() && key[1] == w.getSiblingIndex()))
                .collect(Collectors.toList());

        writingRepository.deleteAll(toDelete); // 진짜 삭제

        return officials;
    }
    
    public List<MyWritingCoverResponseDto> getMyWritingCovers(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("유저 없음"));

        return writingRepository.findMyWritingCovers(user.getId());
    }
     
}
