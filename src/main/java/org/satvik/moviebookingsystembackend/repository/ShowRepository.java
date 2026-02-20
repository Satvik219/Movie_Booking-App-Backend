package org.satvik.moviebookingsystembackend.repository;



import org.satvik.moviebookingsystembackend.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {

    @Query("SELECT s FROM Show s WHERE s.movie.id = :movieId AND s.showDate = :date ORDER BY s.startTime ASC")
    List<Show> findByMovieAndDate(@Param("movieId") Long movieId, @Param("date") LocalDate date);

    @Query("SELECT s FROM Show s WHERE s.theatre.id = :theatreId AND s.showDate = :date ORDER BY s.startTime ASC")
    List<Show> findByTheatreAndDate(@Param("theatreId") Long theatreId, @Param("date") LocalDate date);

    @Query("SELECT s FROM Show s WHERE s.movie.id = :movieId AND s.theatre.id = :theatreId AND s.showDate = :date ORDER BY s.startTime ASC")
    List<Show> findByMovieTheatreAndDate(@Param("movieId") Long movieId, @Param("theatreId") Long theatreId, @Param("date") LocalDate date);

    @Query("SELECT s FROM Show s WHERE s.theatre.city = :city AND s.showDate = :date AND s.status != 'CANCELLED' ORDER BY s.startTime ASC")
    List<Show> findByCityAndDate(@Param("city") String city, @Param("date") LocalDate date);

    @Query("SELECT s FROM Show s WHERE s.movie.id = :movieId AND s.theatre.city = :city AND s.showDate >= :date ORDER BY s.showDate, s.startTime")
    List<Show> findByMovieAndCityFromDate(@Param("movieId") Long movieId, @Param("city") String city, @Param("date") LocalDate date);

    @Query("SELECT s FROM Show s WHERE s.showDate < CURRENT_DATE AND s.status = 'UPCOMING'")
    List<Show> findShowsToMarkCompleted();

    @Modifying
    @Transactional
    @Query("UPDATE Show s SET s.availableSeats = s.availableSeats - :count WHERE s.id = :showId AND s.availableSeats >= :count")
    int decreaseAvailableSeats(@Param("showId") Long showId, @Param("count") int count);

    @Modifying
    @Transactional
    @Query("UPDATE Show s SET s.availableSeats = s.availableSeats + :count WHERE s.id = :showId")
    void increaseAvailableSeats(@Param("showId") Long showId, @Param("count") int count);

    @Query("SELECT s FROM Show s WHERE s.screen.id = :screenId AND s.showDate = :date ORDER BY s.startTime ASC")
    List<Show> findByScreenAndDate(@Param("screenId") Long screenId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(s) FROM Show s WHERE s.movie.id = :movieId AND s.showDate >= CURRENT_DATE")
    Long countUpcomingShowsForMovie(@Param("movieId") Long movieId);
}

