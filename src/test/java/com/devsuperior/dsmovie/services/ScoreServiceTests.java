package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.dto.ScoreDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.entities.ScoreEntity;
import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.repositories.ScoreRepository;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import com.devsuperior.dsmovie.tests.ScoreFactory;
import com.devsuperior.dsmovie.tests.UserFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

@ExtendWith(SpringExtension.class)
public class ScoreServiceTests {
	
	@InjectMocks
	private ScoreService service;

	@Mock
	private UserService userService;

	@Mock
	private ScoreRepository repository;

	@Mock
	private MovieRepository movieRepository;

	private ScoreEntity scoreEntity;
	private ScoreDTO scoreDTO;
	private MovieEntity movieEntity;
	private MovieDTO movieDTO;
	private UserEntity user;
	private Long movieExistingId, movieNonExistingId;

	@BeforeEach
	void setUp() throws Exception{
		scoreEntity = ScoreFactory.createScoreEntity();
		scoreDTO = ScoreFactory.createScoreDTO();
		movieExistingId = 1L;
		movieNonExistingId = 100L;
		movieEntity = MovieFactory.createMovieEntity();
		movieEntity.getScores().add(scoreEntity);
		movieDTO = new MovieDTO(movieEntity);
		user = UserFactory.createUserEntity();

		Mockito.when(repository.saveAndFlush((ScoreEntity) ArgumentMatchers.any())).thenReturn(scoreEntity);
		Mockito.when(movieRepository.findById(movieExistingId)).thenReturn(Optional.of(movieEntity));
		Mockito.when(movieRepository.findById(movieNonExistingId)).thenReturn(Optional.empty());
		Mockito.when(movieRepository.save((MovieEntity) ArgumentMatchers.any())).thenReturn(movieEntity);
		Mockito.when(userService.authenticated()).thenReturn(user);
	}

	@Test
	public void saveScoreShouldReturnMovieDTO() {

		MovieDTO result = service.saveScore(scoreDTO);
		Assertions.assertNotNull(result);
	}
	
	@Test
	public void saveScoreShouldThrowResourceNotFoundExceptionWhenNonExistingMovieId() {
		MovieEntity movie = MovieFactory.createMovieEntity();
		movie.setId(movieNonExistingId);
		scoreEntity.setMovie(movie);
		ScoreDTO dto = new ScoreDTO(scoreEntity);

		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.saveScore(dto);
		});
	}
}
