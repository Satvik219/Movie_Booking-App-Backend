package org.satvik.moviebookingsystembackend.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.satvik.moviebookingsystembackend.entity.Booking;
import org.satvik.moviebookingsystembackend.entity.Payment;
import org.satvik.moviebookingsystembackend.exception.PaymentException;
import org.satvik.moviebookingsystembackend.exception.ResourceNotFoundException;
import org.satvik.moviebookingsystembackend.repository.BookingRepository;
import org.satvik.moviebookingsystembackend.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    @Value("${stripe.api.key}")
    private String stripeSecretKey;

    @Value("${stripe.publishable.key}")
    private String stripePublishableKey;

    /**
     * Creates a Stripe PaymentIntent and saves a PENDING payment record.
     * Returns the Payment with stripeClientSecret so the frontend can complete payment.
     */
    @Transactional
    public Payment createPaymentIntent(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        try {
            Stripe.apiKey = stripeSecretKey;

            // Amount must be in smallest currency unit (paise for INR)
            long amountInPaise = Math.round(booking.getFinalAmount() * 100);

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInPaise)
                    .setCurrency("inr")
                    .setDescription("Movie ticket booking - " + booking.getBookingReference())
                    .putMetadata("bookingId", bookingId.toString())
                    .putMetadata("bookingReference", booking.getBookingReference())
                    .putMetadata("userId", booking.getUser().getId().toString())
                    // automatic_payment_methods lets Stripe show all enabled methods
                    // including Google Pay, Apple Pay, cards — based on user's browser/device
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                    .build()
                    )
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            Payment payment = Payment.builder()
                    .booking(booking)
                    .stripePaymentIntentId(paymentIntent.getId())
                    .stripeClientSecret(paymentIntent.getClientSecret())
                    .amount(booking.getFinalAmount())
                    .currency("INR")
                    .status(Payment.PaymentStatus.PENDING)
                    .build();

            return paymentRepository.save(payment);

        } catch (StripeException e) {
            log.error("Failed to create Stripe PaymentIntent: {}", e.getMessage());
            throw new PaymentException("Failed to create payment: " + e.getMessage());
        }
    }

    /**
     * Verifies a PaymentIntent server-side by retrieving it from Stripe.
     * Only succeeds if Stripe reports status = "succeeded".
     * This is safe — no signature to spoof, Stripe is the source of truth.
     */
    @Transactional
    public boolean verifyAndUpdatePayment(String paymentIntentId) {
        try {
            Stripe.apiKey = stripeSecretKey;

            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found for intent: " + paymentIntentId));

            if (!"succeeded".equals(paymentIntent.getStatus())) {
                log.warn("PaymentIntent {} has status: {}", paymentIntentId, paymentIntent.getStatus());
                paymentRepository.updatePaymentStatus(paymentIntentId, Payment.PaymentStatus.FAILED, null);
                return false;
            }

            // Extract charge ID from the PaymentIntent (the actual charge)
            String chargeId = paymentIntent.getLatestCharge();

            paymentRepository.updatePaymentStatus(paymentIntentId, Payment.PaymentStatus.SUCCESS, chargeId);

            // Confirm booking
            Booking booking = payment.getBooking();
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            log.info("Stripe payment verified and booking confirmed: {}", booking.getBookingReference());
            return true;

        } catch (StripeException e) {
            log.error("Stripe payment verification error: {}", e.getMessage());
            throw new PaymentException("Payment verification failed: " + e.getMessage());
        }
    }

    /**
     * Processes a refund via Stripe using the stored charge ID.
     */
    @Transactional
    public boolean processRefund(Long bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for booking: " + bookingId));

        if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
            throw new PaymentException("Cannot refund payment with status: " + payment.getStatus());
        }

        try {
            Stripe.apiKey = stripeSecretKey;

            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(payment.getStripePaymentIntentId())
                    .setAmount(Math.round(payment.getAmount() * 100)) // paise
                    .build();

            Refund refund = Refund.create(params);

            if (!"succeeded".equals(refund.getStatus())) {
                throw new PaymentException("Refund was not successful: " + refund.getStatus());
            }

            payment.setStatus(Payment.PaymentStatus.REFUNDED);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            log.info("Refund processed for booking {}: refund ID {}", bookingId, refund.getId());
            return true;

        } catch (StripeException e) {
            log.error("Stripe refund failed: {}", e.getMessage());
            throw new PaymentException("Refund failed: " + e.getMessage());
        }
    }

    /**
     * Returns the publishable key so the frontend can initialise Stripe.js
     */
    public String getStripePublishableKey() {
        return stripePublishableKey;
    }
}