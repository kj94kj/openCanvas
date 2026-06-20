package cauCapstone.openCanvas.rdb.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import cauCapstone.openCanvas.rdb.dto.CoverDto;
import cauCapstone.openCanvas.rdb.entity.Content;
import cauCapstone.openCanvas.rdb.entity.ContentGenre;
import cauCapstone.openCanvas.rdb.entity.Genre;
import cauCapstone.openCanvas.rdb.entity.Writing;
import cauCapstone.openCanvas.rdb.repository.ContentGenreRepository;
import cauCapstone.openCanvas.rdb.repository.ContentRepository;
import cauCapstone.openCanvas.rdb.repository.CoverRepository;
import cauCapstone.openCanvas.rdb.repository.GenreRepository;
import cauCapstone.openCanvas.rdb.repository.WritingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GenreService {
	private final GenreRepository genreRepository;
	private final ContentGenreRepository contentGenreRepository;
    private final ContentRepository contentRepository;
    private final CoverRepository coverRepository;
    private final WritingRepository writingRepository;

    // 최초 작성자만 장르를 세팅 가능.
    @Transactional
	public List<String> setGenre(String email, List<String> genres, Long contentId) {
    	
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("Content not found"));
        
        Writing firstW = writingRepository
                .findByDepthAndSiblingIndexAndContent_Title(1, 1, content.getTitle())
                .orElseThrow(() ->  new IllegalArgumentException("존재하지 않는 writing입니다."));
        
        if(! (firstW.getUser().getEmail().equals(email))) {
        	throw new IllegalArgumentException("유저가 루트유저가 아닙니다.");
        }
		
        for (String genreName : genres) {
        	
            Genre genre = genreRepository.findByName(genreName)
                .orElseGet(() -> {
                    Genre newGenre = new Genre();
                    newGenre.setName(genreName);
                    Genre g = genreRepository.save(newGenre);
                    
                    return g;
                });

            ContentGenre contentGenre = new ContentGenre();
            contentGenre.setContent(content);
            contentGenre.setGenre(genre);
            contentGenreRepository.save(contentGenre);
        }
 
        return genres;
		
	}
	
	public List<String> getAllGenres(){
        return genreRepository.findAll()
                .stream()
                .map(Genre::getName)
                .collect(Collectors.toList());
	}
	
	public List<String> getGenre (Long contentId){
	    return contentGenreRepository.findGenreNamesByContentId(contentId);
	}
	
	public List<CoverDto> searchGenre(String genreName){
	    return coverRepository.findCoverDtosByGenreName(genreName);
		
	}
}
