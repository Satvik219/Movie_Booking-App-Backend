package org.satvik.moviebookingsystembackend.dto;

import org.satvik.moviebookingsystembackend.entity.Booking;
import org.satvik.moviebookingsystembackend.entity.Payment;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class BookingDTO {

    @Data
    public static class BookingRequest {
        private Long showId;
        private List<Long> seatIds;
    }

    @Data
    public static class BookingResponse {
        private Long id;
        private String bookingReference;
        private String movieTitle;
        private String theatreName;
        private String showDate;
        private String showTime;
        private List<String> seats;
        private Double totalAmount;
        private Double convenienceFee;
        private Double finalAmount;
        private Booking.BookingStatus status;
        private LocalDateTime bookedAt;
        private String razorpayOrderId;
        private String razorpayKeyId;
    }

    @Data
    public static class PaymentVerificationRequest {
        private String razorpayOrderId;
        private String razorpayPaymentId;
        private String razorpaySignature;
        private Long bookingId;
    }

    @Data
    public static class PaymentResponse {
        private Long paymentId;
        private String razorpayOrderId;
        private String razorpayPaymentId;
        private Double amount;
        private Payment.PaymentStatus status;
        private String bookingReference;
    }

    @Data
    public static class CancelBookingRequest {
        private Long bookingId;
        private String reason;
    }
}
