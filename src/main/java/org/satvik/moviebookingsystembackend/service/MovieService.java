package org.satvik.moviebookingsystembackend.service;
import org.satvik.moviebookingsystembackend.dto.MovieDTO;
import org.satvik.moviebookingsystembackend.entity.Movie;
import org.satvik.moviebookingsystembackend.exception.ResourceNotFoundException;
import org.satvik.moviebookingsystembackend.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    public List<MovieDTO.MovieResponse> getAllActiveMovies() {
        return movieRepository.findAllActiveMovies().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public MovieDTO.MovieResponse getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
        return mapToResponse(movie);
    }

    public List<MovieDTO.MovieResponse> searchMovies(String title) {
        return movieRepository.searchByTitle(title).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<MovieDTO.MovieResponse> getMoviesByGenre(String genre) {
        return movieRepository.findByGenre(genre).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<MovieDTO.MovieResponse> getNowPlayingInCity(String city) {
        return movieRepository.findNowPlayingInCity(city, LocalDate.now()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<MovieDTO.MovieResponse> getUpcomingMovies() {
        return movieRepository.findUpcomingMovies().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<MovieDTO.MovieResponse> getTopRatedMovies(int limit) {
        return movieRepository.findTopRatedMovies(limit).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<String> getAllGenres() {
        return movieRepository.findAllDistinctGenres();
    }

    @Transactional
    public MovieDTO.MovieResponse createMovie(MovieDTO.MovieRequest request) {
        Movie movie = Movie.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .genre(request.getGenre())
                .language(request.getLanguage())
                .director(request.getDirector())
                .cast(request.getCast())
                .durationMinutes(request.getDurationMinutes())
                .releaseDate(request.getReleaseDate())
                .posterUrl(request.getPosterUrl())
                .trailerUrl(request.getTrailerUrl())
                .rating(request.getRating())
                .certificate(request.getCertificate())
                .build();
        return mapToResponse(movieRepository.save(movie));
    }

    @Transactional
    public MovieDTO.MovieResponse updateMovie(Long id, MovieDTO.MovieRequest request) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));

        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setGenre(request.getGenre());
        movie.setLanguage(request.getLanguage());
        movie.setDirector(request.getDirector());
        movie.setCast(request.getCast());
        movie.setDurationMinutes(request.getDurationMinutes());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setPosterUrl(request.getPosterUrl());
        movie.setTrailerUrl(request.getTrailerUrl());
        movie.setRating(request.getRating());
        movie.setCertificate(request.getCertificate());

        return mapToResponse(movieRepository.save(movie));
    }

    @Transactional
    public void deleteMovie(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
        movie.setActive(false);
        movieRepository.save(movie);
    }

    private MovieDTO.MovieResponse mapToResponse(Movie movie) {
        MovieDTO.MovieResponse response = new MovieDTO.MovieResponse();
        response.setId(movie.getId());
        response.setTitle(movie.getTitle());
        response.setDescription(movie.getDescription());
        response.setGenre(movie.getGenre());
        response.setLanguage(movie.getLanguage());
        response.setDirector(movie.getDirector());
        response.setCast(movie.getCast());
        response.setDurationMinutes(movie.getDurationMinutes());
        response.setReleaseDate(movie.getReleaseDate());
        response.setPosterUrl(movie.getPosterUrl());
        response.setTrailerUrl(movie.getTrailerUrl());
        response.setRating(movie.getRating());
        response.setCertificate(movie.getCertificate());
        response.setActive(movie.isActive());
        return response;
    }
}

