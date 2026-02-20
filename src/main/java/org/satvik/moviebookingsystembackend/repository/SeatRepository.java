package org.satvik.moviebookingsystembackend.repository;

import org.satvik.moviebookingsystembackend.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    @Query("SELECT s FROM Seat s WHERE s.screen.id = :screenId ORDER BY s.rowNumber, s.seatNumber")
    List<Seat> findByScreenId(@Param("screenId") Long screenId);

    @Query("SELECT s FROM Seat s WHERE s.screen.id = :screenId AND s.seatType = :type ORDER BY s.rowNumber, s.seatNumber")
    List<Seat> findByScreenIdAndType(@Param("screenId") Long screenId, @Param("type") Seat.SeatType type);

    @Query("SELECT COUNT(s) FROM Seat s WHERE s.screen.id = :screenId")
    Long countByScreenId(@Param("screenId") Long screenId);

    @Query("SELECT s FROM Seat s WHERE s.screen.id = :screenId AND s.rowNumber = :row ORDER BY s.seatNumber")
    List<Seat> findByScreenIdAndRow(@Param("screenId") Long screenId, @Param("row") String row);
}

