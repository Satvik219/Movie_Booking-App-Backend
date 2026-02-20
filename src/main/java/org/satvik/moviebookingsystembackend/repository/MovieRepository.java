package org.satvik.moviebookingsystembackend.repository;

import org.satvik.moviebookingsystembackend.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    @Query("SELECT m FROM Movie m WHERE m.active = true ORDER BY m.releaseDate DESC")
    List<Movie> findAllActiveMovies();

    @Query("SELECT m FROM Movie m WHERE m.title LIKE %:title% AND m.active = true")
    List<Movie> searchByTitle(@Param("title") String title);

    @Query("SELECT m FROM Movie m WHERE m.genre = :genre AND m.active = true")
    List<Movie> findByGenre(@Param("genre") String genre);

    @Query("SELECT m FROM Movie m WHERE m.language = :language AND m.active = true")
    List<Movie> findByLanguage(@Param("language") String language);

    @Query("SELECT m FROM Movie m WHERE m.releaseDate = :date AND m.active = true")
    List<Movie> findByReleaseDate(@Param("date") LocalDate date);

    @Query("SELECT DISTINCT m FROM Movie m JOIN m.shows s WHERE s.showDate >= :date AND s.theatre.city = :city AND m.active = true")
    List<Movie> findNowPlayingInCity(@Param("city") String city, @Param("date") LocalDate date);

    @Query("SELECT m FROM Movie m WHERE m.rating >= :rating AND m.active = true ORDER BY m.rating DESC")
    List<Movie> findByMinRating(@Param("rating") Double rating);

    @Query("SELECT m FROM Movie m WHERE m.releaseDate > CURRENT_DATE AND m.active = true ORDER BY m.releaseDate ASC")
    List<Movie> findUpcomingMovies();

    @Query("SELECT m FROM Movie m WHERE m.certificate = :certificate AND m.active = true")
    List<Movie> findByCertificate(@Param("certificate") Movie.Certificate certificate);

    @Query("SELECT COUNT(m) FROM Movie m WHERE m.active = true")
    Long countActiveMovies();

    @Query(value = "SELECT * FROM movies ORDER BY rating DESC LIMIT :limit", nativeQuery = true)
    List<Movie> findTopRatedMovies(@Param("limit") int limit);

    @Query("SELECT DISTINCT m.genre FROM Movie m WHERE m.active = true")
    List<String> findAllDistinctGenres();
}

