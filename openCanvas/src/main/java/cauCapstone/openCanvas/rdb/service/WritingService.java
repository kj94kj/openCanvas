package cauCapstone.openCanvas.rdb.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import cauCapstone.openCanvas.openai.coverimage.service.CoverImageService;
import cauCapstone.openCanvas.rdb.dto.ContentDto;
import cauCapstone.openCanvas.rdb.dto.WritingDto;
import cauCapstone.openCanvas.rdb.entity.Content;
import cauCapstone.openCanvas.rdb.entity.Genre;
import cauCapstone.openCanvas.rdb.entity.Role;
import cauCapstone.openCanvas.rdb.entity.RoomType;
import cauCapstone.openCanvas.rdb.entity.User;
import cauCapstone.openCanvas.rdb.entity.Writing;
import cauCapstone.openCanvas.rdb.repository.ContentGenreRepository;
import cauCapstone.openCanvas.rdb.repository.ContentRepository;
import cauCapstone.openCanvas.rdb.repository.CoverRepository;
import cauCapstone.openCanvas.rdb.repository.GenreRepository;
import cauCapstone.openCanvas.rdb.repository.UserRepository;
import cauCapstone.openCanvas.rdb.repository.WritingRepository;
import cauCapstone.openCanvas.recommend.service.RecommendService;
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
    private final RecommendService recommendService;
    private final CoverImageService coverImageService;
    private final CoverRepository coverRepository;
	private final ContentGenreRepository contentGenreRepository;
	private final GenreRepository genreRepository;

    // 현재 depth로 글을 써도 되는지 체크함.
    // 체크하고 문서방 만들기.
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
    
    // ! 유저 필요한지 체크
    // depth랑 siblingindex, title 받아서 저장하기: 검증절차는 거쳤다고 판단하고 검증 안하고 저장함.
    public Writing saveWriting(WritingDto writingDto) {
        // 1. User 조회
        User user = userRepository.findByEmail(writingDto.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 2. Content 조회
        Content content = contentRepository.findByTitle(writingDto.getTitle())
            .orElseThrow(() -> new IllegalArgumentException("콘텐츠를 찾을 수 없습니다."));

        // 3. 부모 Writing 조회 (루트가 아니면)
        Writing parent = null;
        if (writingDto.getDepth() > 1) {
            parent = writingRepository
                .findByDepthAndSiblingIndexAndContent_Title(writingDto.getDepth()-1, writingDto.getParentSiblingIndex(), 
                		content.getTitle())
                .orElseThrow(() -> new IllegalArgumentException("부모 글이 존재하지 않습니다."));
        }

        // 4. Writing 저장
        Writing writing = writingDto.toEntity(user, content, parent);
        
        if(parent.getDepth() >= content.getCover().getLimit()) {
        	content.getCover().setRoomType(RoomType.COMPLETE);
        	coverRepository.save(content.getCover());
        	
        }

        return writingRepository.save(writing);
    }
    
    // !유저 필요
    // 루트 사용자(맨처음 글을 쓴 사람) 확인하고 삭제하기: 실제 삭제가 아니라 내용만 빈 내용으로 바꿈(추후에 내용을 변경할 수도 있겠다).
    // 현재유저의 dto와 지우고싶은 writingDto를 받음.
    // TODO: 글을 쓴 사람은 변경 하지 않았는데, 안보이게 하는 조치가 필요함.
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
    
    // 글(content)의 모든 버전 가져오기
    // w.depth, w.siblingIndex, w.time, u.email만 가져오게됨
    // TODO: contentTitle만으로 충분하긴한데 확인해보기.
    public List<WritingDto> getSimpleWriting(String title){
    	 return writingRepository.findAllDtosByContentTitle(title);
    }
    
    // ! 유저필요
	// ADMIN 유저가 글을 지울 때 쓰는 메소드.
    // ADMIN 유저의 UserDto와 삭제할 글의 WritingDto를 받음.
    // 삭제처리(공백처리)된 글의 글쓴이는 admin으로 임시지정함.
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
    
    // TODO: 여기 컨트롤러로써야함.
    // 문서방에 들어가는 유저가(구독하는 유저) roomId를 받고 보여줘야하는 writingDto 리턴함.
    // 글을 ChatRoomRedisEntity에 저장된 version이 글을 쓰려고 하는 Writing의 version이기 때문에 그것의 부모의 글부터 가져와야한다.
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
    
    // String으로 되있던 버전을 List<Integer>로 리턴함.
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
        
        // 3. 작성자 확인
        if (!email.equals(root.getUser().getEmail())) {
            throw new IllegalArgumentException("해당 콘텐츠의 작성자만 official을 설정할 수 있습니다.");
        }
        

        // 4. Content 조회 및 버전 문자열 생성
        Content content = current.getContent();
        String versionStr = current.getDepth() + "." + current.getSiblingIndex();

        if (current.getParent() != null) {
            versionStr = versionStr + "." + current.getParent().getSiblingIndex();
        }

        // 5. Content official 필드 설정 및 저장
        content.setOfficial(versionStr);
        
        // TODO: 필요시 기존에 content에 붙어있던 태그 삭제, 새로운 tag를 부여해줘야함
        
        // 추천서버에 기존글 삭제, 태그 저장한 새 글 등록(List<Writing>으로 저장했던 글을 String content로 풀어저장)
        recommendService.deleteItem(content.getId());
        
        List<WritingDto> wOfficial = getWritingWithParents(writingDto);
        
        // 전체 텍스트 빌드
        StringBuilder textBuilder = new StringBuilder();
        for (WritingDto dto : wOfficial) {
            textBuilder.append(dto.getBody()).append("\n");
        }
        
        // 장르목록
		List<String> genreNames = contentGenreRepository.findGenreNamesByContentId(content.getId());
		
        // 장르 이름을 기반으로 장르 ID 리스트로 변환
		List<Integer> tagIds = genreNames.stream()
			    .map(name -> genreRepository.findByName(name))
			    .filter(Optional::isPresent)
			    .map(opt -> opt.get().getId().intValue())  // 여기서 Long → int 변환
			    .collect(Collectors.toList());
        
        Map<String, Object> itemRequest = Map.of(
                "id", content.getId(), 
                "title", content.getTitle(),
                "text", textBuilder.toString().trim(),
                "tags", tagIds
            );
        recommendService.createItem(itemRequest);
        
        
        
        // 조회수 1000 이상 작품 일러스트 생성
        // TODO: 장르목록 확인, List -> array로 바꾼거 확인
        if (content.getView() >= 1000) {
            coverImageService.makeImageAndSave(
                Long.toString(content.getId()),
                content.getTitle(),
                genreNames.toArray(new String[0]),
                textBuilder.toString().trim()
            );
        }

        contentRepository.save(content);

        // 6. 공식 버전 기준 트리 반환
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
        
        
        // 3. 전체 글 가져오기
        List<Writing> allWritings = writingRepository.findAllByContent_Title(contentDto.getTitle());

        // 4. official에 포함된 글은 유지, 나머지는 삭제
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
     
    /*
    // 루트 사용자를 확인하고 삭제할 글을 다른곳에 이어붙인다.
    // 삭제할 dto를 매개변수로 받아야한다.
    public boolean reattachWriting(WritingDto writingDto, UserDto userDto, int newParentDepth, int newParentSiblingIndex) {
    	Writing userWriting = writingRepository.findByUserNameAndTitle(userDto.getEmail(), writingDto.getTitle())
	            .orElseThrow(() -> new IllegalArgumentException("유저가 쓴 writing을 찾을 수 없습니다."));
    	
    	if(userWriting.getDepth() == 1) {
        	Writing willDelete = writingRepository.findByDepthAndSiblingIndexAndContentId(writingDto.getDepth(), 
        			writingDto.getSiblingIndex(), writingDto.getTitle())
    	            .orElseThrow(() -> new IllegalArgumentException("삭제할 writing을 찾을 수 없습니다."));
        	
        	List<Writing> childs = writingRepository.findAllByParent(willDelete);
        	
        	if(childs.size() > 0) {
        		if(newParentDepth == willDelete.getDepth()) {
        	    	Writing newParent = writingRepository.findByDepthAndSiblingIndexAndContentId(
        	    			newParentDepth, newParentSiblingIndex, writingDto.getTitle())
        		            .orElseThrow(() -> new IllegalArgumentException("새 부모 writing을 찾을 수 없습니다."));
        	    	
        	    	for(Writing c: childs) {
        	    		c.setParent(newParent);
        	    	}
        		}else if(newParentDepth == willDelete.getDepth() - 1){
        			
        		}else {
        			throw new IllegalArgumentException("잘못된 parentDepth 설정입니다.");
        		}
        	}
        	
    	}else {
    		throw new IllegalArgumentException("유저가 루트가 아닙니다.");
    	}
 	
    }
    */
}
