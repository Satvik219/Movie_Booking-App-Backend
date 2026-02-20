package org.satvik.moviebookingsystembackend.repository;

import org.satvik.moviebookingsystembackend.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId ORDER BY b.bookedAt DESC")
    List<Booking> findByUserId(@Param("userId") Long userId);

    @Query("SELECT b FROM Booking b WHERE b.bookingReference = :ref")
    Optional<Booking> findByBookingReference(@Param("ref") String bookingReference);

    @Query("SELECT b FROM Booking b WHERE b.show.id = :showId AND b.status != 'CANCELLED'")
    List<Booking> findConfirmedBookingsByShow(@Param("showId") Long showId);

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.status = :status ORDER BY b.bookedAt DESC")
    List<Booking> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Booking.BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.status = 'PENDING' AND b.bookedAt < :cutoffTime")
    List<Booking> findExpiredPendingBookings(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT SUM(b.finalAmount) FROM Booking b WHERE b.status = 'CONFIRMED' AND b.bookedAt BETWEEN :start AND :end")
    Double getTotalRevenue(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.show.movie.id = :movieId AND b.status = 'CONFIRMED'")
    Long countConfirmedBookingsByMovie(@Param("movieId") Long movieId);

    @Query("SELECT b FROM Booking b WHERE b.show.movie.id = :movieId AND b.status = 'CONFIRMED' ORDER BY b.bookedAt DESC")
    List<Booking> findByMovieId(@Param("movieId") Long movieId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.bookedAt BETWEEN :start AND :end AND b.status = 'CONFIRMED'")
    Long countBookingsInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "SELECT DATE(booked_at) as date, COUNT(*) as count, SUM(final_amount) as revenue " +
            "FROM bookings WHERE status = 'CONFIRMED' AND booked_at >= :startDate " +
            "GROUP BY DATE(booked_at) ORDER BY date", nativeQuery = true)
    List<Object[]> getDailyBookingStats(@Param("startDate") LocalDateTime startDate);
}

