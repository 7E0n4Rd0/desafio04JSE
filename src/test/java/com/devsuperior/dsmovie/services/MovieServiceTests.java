package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.services.exceptions.DatabaseException;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
public class MovieServiceTests {
	
	@InjectMocks
	private MovieService service;

	@Mock
	private MovieRepository repository;

	private MovieEntity movie;
	private MovieDTO movieDTO;
	private Page<MovieDTO> pageDto;
	private Page<MovieEntity> page;
	private Pageable pageable;
	private String title;
	private Long movieExistingId, movieNonExistingId, movieDependentId;

	@BeforeEach
	void setUp() throws Exception{
		movieExistingId = 1L;
		movieNonExistingId = 1000L;
		movieDependentId = 2L;
		movie = MovieFactory.createMovieEntity();
		movieDTO = new MovieDTO(movie);
		title = "Whiplash";
		pageable = PageRequest.of(0, 12);
		page = new PageImpl<>(List.of(movie), pageable, 1);
		pageDto = page.map(MovieDTO::new);

		Mockito.when(repository.existsById(movieExistingId)).thenReturn(true);
		Mockito.when(repository.existsById(movieNonExistingId)).thenReturn(false);
		Mockito.when(repository.existsById(movieDependentId)).thenReturn(true);

		Mockito.when(repository.searchByTitle(title, pageable)).thenReturn(page);
		Mockito.when(repository.findById(movieExistingId)).thenReturn(Optional.of(movie));
		Mockito.when(repository.findById(movieNonExistingId)).thenReturn(Optional.empty());
		Mockito.when(repository.save((MovieEntity) ArgumentMatchers.any())).thenReturn(movie);
		Mockito.when(repository.getReferenceById(movieExistingId)).thenReturn(movie);
		Mockito.when(repository.getReferenceById(movieNonExistingId)).thenThrow(EntityNotFoundException.class);
		Mockito.doNothing().when(repository).deleteById(movieExistingId);
		Mockito.doThrow(EntityNotFoundException.class).when(repository).deleteById(movieNonExistingId);
		Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(movieDependentId);

	}
	
	@Test
	public void findAllShouldReturnPagedMovieDTO() {
		Page<MovieDTO> result = service.findAll(title, pageable);
		Assertions.assertNotNull(result);
		Assertions.assertEquals(0, result.getNumber());
		Assertions.assertEquals(1, result.getContent().size());
		Assertions.assertEquals("Test Movie", result.getContent().getFirst().getTitle());
	}
	
	@Test
	public void findByIdShouldReturnMovieDTOWhenIdExists() {
		MovieDTO result = service.findById(movieExistingId);
		Assertions.assertNotNull(result);
		Assertions.assertEquals(1L, result.getId());
		Assertions.assertEquals("Test Movie", result.getTitle());
	}
	
	@Test
	public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.findById(movieNonExistingId);
		});
	}
	
	@Test
	public void insertShouldReturnMovieDTO() {
		MovieDTO dto = MovieFactory.createMovieDTO();
		MovieDTO result = service.insert(dto);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(dto.getId(), result.getId());
		Assertions.assertEquals(dto.getTitle(), result.getTitle());
		Assertions.assertEquals(dto.getCount(), result.getCount());
		Assertions.assertEquals(dto.getScore(), result.getScore());
		Assertions.assertEquals(dto.getImage(), result.getImage());
	}
	
	@Test
	public void updateShouldReturnMovieDTOWhenIdExists() {
		MovieEntity entity = MovieFactory.createMovieEntity();
		entity.setTitle("Whiplash");
		MovieDTO dto = new MovieDTO(entity);
		MovieDTO result = service.update(movieExistingId, dto);
		Assertions.assertNotNull(result);
		Assertions.assertEquals(entity.getTitle(), result.getTitle());

	}
	
	@Test
	public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.update(movieNonExistingId, movieDTO);
		});
	}
	
	@Test
	public void deleteShouldDoNothingWhenIdExists() {
		Assertions.assertDoesNotThrow(() -> service.delete(movieExistingId));
	}
	
	@Test
	public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.delete(movieNonExistingId);
		});
	}
	
	@Test
	public void deleteShouldThrowDatabaseExceptionWhenDependentId() {
		Assertions.assertThrows(DatabaseException.class, () -> {
			service.delete(movieDependentId);
		});
	}
}
