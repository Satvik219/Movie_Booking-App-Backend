package org.satvik.moviebookingsystembackend.repository;

import org.satvik.moviebookingsystembackend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT p FROM Payment p WHERE p.booking.id = :bookingId")
    Optional<Payment> findByBookingId(@Param("bookingId") Long bookingId);

    @Query("SELECT p FROM Payment p WHERE p.razorpayOrderId = :orderId")
    Optional<Payment> findByRazorpayOrderId(@Param("orderId") String orderId);

    @Query("SELECT p FROM Payment p WHERE p.razorpayPaymentId = :paymentId")
    Optional<Payment> findByRazorpayPaymentId(@Param("paymentId") String paymentId);

    @Query("SELECT p FROM Payment p WHERE p.status = :status ORDER BY p.createdAt DESC")
    List<Payment> findByStatus(@Param("status") Payment.PaymentStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE Payment p SET p.status = :status, p.razorpayPaymentId = :paymentId, p.razorpaySignature = :signature, p.updatedAt = CURRENT_TIMESTAMP WHERE p.razorpayOrderId = :orderId")
    void updatePaymentStatus(@Param("orderId") String orderId,
                             @Param("status") Payment.PaymentStatus status,
                             @Param("paymentId") String paymentId,
                             @Param("signature") String signature);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'SUCCESS'")
    Double getTotalSuccessfulPayments();

    @Query("SELECT p FROM Payment p WHERE p.booking.user.id = :userId ORDER BY p.createdAt DESC")
    List<Payment> findByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
    Long countByStatus(@Param("status") Payment.PaymentStatus status);
}

