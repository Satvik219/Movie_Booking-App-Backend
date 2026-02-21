package org.satvik.moviebookingsystembackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    // Stripe PaymentIntent ID (replaces razorpay_order_id)
    @Column(name = "stripe_payment_intent_id", unique = true)
    private String stripePaymentIntentId;

    // Stripe Charge ID (replaces razorpay_payment_id)
    @Column(name = "stripe_charge_id")
    private String stripeChargeId;

    // Stripe client secret — sent to frontend to complete payment
    @Column(name = "stripe_client_secret")
    private String stripeClientSecret;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum PaymentStatus {
        PENDING, SUCCESS, FAILED, REFUNDED
    }

    public enum PaymentMethod {
        CARD, UPI, NET_BANKING, WALLET
    }
}