package org.satvik.moviebookingsystembackend.repository;



import org.satvik.moviebookingsystembackend.entity.ShowSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

    @Query("SELECT ss FROM ShowSeat ss WHERE ss.show.id = :showId ORDER BY ss.seat.rowNumber, ss.seat.seatNumber")
    List<ShowSeat> findByShowId(@Param("showId") Long showId);

    @Query("SELECT ss FROM ShowSeat ss WHERE ss.show.id = :showId AND ss.status = :status")
    List<ShowSeat> findByShowIdAndStatus(@Param("showId") Long showId, @Param("status") ShowSeat.SeatStatus status);

    @Query("SELECT ss FROM ShowSeat ss WHERE ss.show.id = :showId AND ss.seat.id = :seatId")
    Optional<ShowSeat> findByShowIdAndSeatId(@Param("showId") Long showId, @Param("seatId") Long seatId);

    @Query("SELECT ss FROM ShowSeat ss WHERE ss.show.id = :showId AND ss.seat.id IN :seatIds")
    List<ShowSeat> findByShowIdAndSeatIds(@Param("showId") Long showId, @Param("seatIds") List<Long> seatIds);

    @Query("SELECT COUNT(ss) FROM ShowSeat ss WHERE ss.show.id = :showId AND ss.status = 'AVAILABLE'")
    Long countAvailableSeats(@Param("showId") Long showId);

    @Modifying
    @Transactional
    @Query("UPDATE ShowSeat ss SET ss.status = :status WHERE ss.show.id = :showId AND ss.seat.id IN :seatIds")
    int updateSeatStatus(@Param("showId") Long showId, @Param("seatIds") List<Long> seatIds, @Param("status") ShowSeat.SeatStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE ShowSeat ss SET ss.status = 'AVAILABLE', ss.booking = null WHERE ss.booking.id = :bookingId")
    void releaseSeatsByBookingId(@Param("bookingId") Long bookingId);

    @Query("SELECT ss FROM ShowSeat ss WHERE ss.booking.id = :bookingId")
    List<ShowSeat> findByBookingId(@Param("bookingId") Long bookingId);

    @Query("SELECT ss FROM ShowSeat ss WHERE ss.show.id = :showId AND ss.seat.seatType = :type")
    List<ShowSeat> findByShowIdAndSeatType(@Param("showId") Long showId, @Param("type") org.satvik.moviebookingsystembackend.entity.Seat.SeatType type);
}

