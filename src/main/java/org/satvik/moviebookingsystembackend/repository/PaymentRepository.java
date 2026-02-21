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

    @Query("SELECT p FROM Payment p WHERE p.stripePaymentIntentId = :paymentIntentId")
    Optional<Payment> findByStripePaymentIntentId(@Param("paymentIntentId") String paymentIntentId);

    @Query("SELECT p FROM Payment p WHERE p.stripeChargeId = :chargeId")
    Optional<Payment> findByStripeChargeId(@Param("chargeId") String chargeId);

    @Query("SELECT p FROM Payment p WHERE p.status = :status ORDER BY p.createdAt DESC")
    List<Payment> findByStatus(@Param("status") Payment.PaymentStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE Payment p SET p.status = :status, p.stripeChargeId = :chargeId, p.updatedAt = CURRENT_TIMESTAMP WHERE p.stripePaymentIntentId = :paymentIntentId")
    void updatePaymentStatus(@Param("paymentIntentId") String paymentIntentId,
                             @Param("status") Payment.PaymentStatus status,
                             @Param("chargeId") String chargeId);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'SUCCESS'")
    Double getTotalSuccessfulPayments();

    @Query("SELECT p FROM Payment p WHERE p.booking.user.id = :userId ORDER BY p.createdAt DESC")
    List<Payment> findByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
    Long countByStatus(@Param("status") Payment.PaymentStatus status);
}