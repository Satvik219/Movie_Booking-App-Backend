package org.satvik.moviebookingsystembackend.repository;


import org.satvik.moviebookingsystembackend.entity.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TheatreRepository extends JpaRepository<Theatre, Long> {

    @Query("SELECT DISTINCT t FROM Theatre t LEFT JOIN FETCH t.screens WHERE t.active = true ORDER BY t.name ASC")
    List<Theatre> findAllActiveTheatres();

    @Query("SELECT t FROM Theatre t WHERE t.city = :city AND t.active = true ORDER BY t.name ASC")
    List<Theatre> findByCity(@Param("city") String city);

    @Query("SELECT t FROM Theatre t WHERE t.name LIKE %:name% AND t.active = true")
    List<Theatre> searchByName(@Param("name") String name);

    @Query("SELECT DISTINCT t.city FROM Theatre t WHERE t.active = true ORDER BY t.city ASC")
    List<String> findAllActiveCities();

    @Query("SELECT DISTINCT t FROM Theatre t JOIN t.shows s WHERE s.movie.id = :movieId AND s.showDate >= CURRENT_DATE AND t.active = true")
    List<Theatre> findTheatresByMovie(@Param("movieId") Long movieId);

    @Query("SELECT t FROM Theatre t WHERE t.city = :city AND EXISTS (SELECT s FROM Show s WHERE s.theatre = t AND s.movie.id = :movieId AND s.showDate >= CURRENT_DATE)")
    List<Theatre> findTheatresByMovieAndCity(@Param("movieId") Long movieId, @Param("city") String city);

    @Query("SELECT COUNT(t) FROM Theatre t WHERE t.active = true AND t.city = :city")
    Long countTheatresByCity(@Param("city") String city);
}

