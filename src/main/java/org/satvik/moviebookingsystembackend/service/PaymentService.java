package org.satvik.moviebookingsystembackend.service;


import org.satvik.moviebookingsystembackend.entity.Booking;
import org.satvik.moviebookingsystembackend.entity.Payment;
import org.satvik.moviebookingsystembackend.exception.PaymentException;
import org.satvik.moviebookingsystembackend.exception.ResourceNotFoundException;
import org.satvik.moviebookingsystembackend.repository.BookingRepository;
import org.satvik.moviebookingsystembackend.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    @Value("${stripe.publishable.key}")
    private String razorpayKeyId;

    @Value("${stripe.api.key}")
    private String razorpayKeySecret;

    @Transactional
    public Payment createRazorpayOrder(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        try {
            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", (int) (booking.getFinalAmount() * 100)); // amount in paise
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", booking.getBookingReference());

            JSONObject notes = new JSONObject();
            notes.put("bookingId", bookingId.toString());
            notes.put("userId", booking.getUser().getId().toString());
            orderRequest.put("notes", notes);

            Order razorpayOrder = razorpay.orders.create(orderRequest);

            Payment payment = Payment.builder()
                    .booking(booking)
                    .razorpayOrderId(razorpayOrder.get("id"))
                    .amount(booking.getFinalAmount())
                    .currency("INR")
                    .status(Payment.PaymentStatus.PENDING)
                    .build();

            return paymentRepository.save(payment);

        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay order: {}", e.getMessage());
            throw new PaymentException("Failed to create payment order: " + e.getMessage());
        }
    }

    @Transactional
    public boolean verifyAndUpdatePayment(String razorpayOrderId, String razorpayPaymentId,
                                          String razorpaySignature) {
        try {
            // Verify signature
            String payload = razorpayOrderId + "|" + razorpayPaymentId;
            String generatedSignature = generateHmacSha256(payload, razorpayKeySecret);

            if (!generatedSignature.equals(razorpaySignature)) {
                log.warn("Payment signature verification failed for order: {}", razorpayOrderId);
                paymentRepository.updatePaymentStatus(razorpayOrderId,
                        Payment.PaymentStatus.FAILED, razorpayPaymentId, razorpaySignature);
                return false;
            }

            // Update payment status
            paymentRepository.updatePaymentStatus(razorpayOrderId,
                    Payment.PaymentStatus.SUCCESS, razorpayPaymentId, razorpaySignature);

            // Update booking status
            Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

            Booking booking = payment.getBooking();
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            log.info("Payment verified and booking confirmed: {}", booking.getBookingReference());
            return true;

        } catch (Exception e) {
            log.error("Payment verification error: {}", e.getMessage());
            throw new PaymentException("Payment verification failed: " + e.getMessage());
        }
    }

    @Transactional
    public boolean processRefund(Long bookingId) {
        try {
            Payment payment = paymentRepository.findByBookingId(bookingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found for booking: " + bookingId));

            if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
                throw new PaymentException("Cannot refund payment with status: " + payment.getStatus());
            }

            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", (int) (payment.getAmount() * 100));
            refundRequest.put("speed", "normal");

            razorpay.payments.refund(payment.getRazorpayPaymentId(), refundRequest);

            payment.setStatus(Payment.PaymentStatus.REFUNDED);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            return true;

        } catch (RazorpayException e) {
            log.error("Refund failed: {}", e.getMessage());
            throw new PaymentException("Refund failed: " + e.getMessage());
        }
    }

    private String generateHmacSha256(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public String getRazorpayKeyId() {
        return razorpayKeyId;
    }
}

