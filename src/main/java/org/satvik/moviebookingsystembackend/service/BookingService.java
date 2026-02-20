package org.satvik.moviebookingsystembackend.service;

import org.satvik.moviebookingsystembackend.dto.BookingDTO;
import org.satvik.moviebookingsystembackend.entity.*;
import org.satvik.moviebookingsystembackend.exception.BookingException;
import org.satvik.moviebookingsystembackend.exception.ResourceNotFoundException;
import org.satvik.moviebookingsystembackend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;

    private static final double CONVENIENCE_FEE_PERCENT = 0.02; // 2%

    @Transactional
    public BookingDTO.BookingResponse initiateBooking(Long userId, BookingDTO.BookingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Show not found"));

        if (show.getStatus() == Show.ShowStatus.CANCELLED || show.getStatus() == Show.ShowStatus.COMPLETED) {
            throw new BookingException("Show is not available for booking");
        }

        // Check and lock seats
        List<ShowSeat> showSeats = showSeatRepository.findByShowIdAndSeatIds(
                request.getShowId(), request.getSeatIds());

        if (showSeats.size() != request.getSeatIds().size()) {
            throw new BookingException("Some seats are not available");
        }

        boolean anyNotAvailable = showSeats.stream()
                .anyMatch(ss -> ss.getStatus() != ShowSeat.SeatStatus.AVAILABLE);
        if (anyNotAvailable) {
            throw new BookingException("One or more selected seats are already booked");
        }

        // Lock seats
        showSeatRepository.updateSeatStatus(request.getShowId(), request.getSeatIds(),
                ShowSeat.SeatStatus.LOCKED);

        // Calculate amounts
        double totalAmount = showSeats.stream().mapToDouble(ShowSeat::getPrice).sum();
        double convenienceFee = Math.ceil(totalAmount * CONVENIENCE_FEE_PERCENT);
        double finalAmount = totalAmount + convenienceFee;

        // Create booking
        Booking booking = Booking.builder()
                .bookingReference("MBK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .user(user)
                .show(show)
                .totalAmount(totalAmount)
                .convenienceFee(convenienceFee)
                .finalAmount(finalAmount)
                .status(Booking.BookingStatus.PENDING)
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        // Link seats to booking
        showSeats.forEach(ss -> ss.setBooking(savedBooking));
        showSeatRepository.saveAll(showSeats);

        // Create Razorpay order
        Payment payment = paymentService.createRazorpayOrder(savedBooking.getId());

        // Decrease available seats
        showRepository.decreaseAvailableSeats(show.getId(), request.getSeatIds().size());

        return buildBookingResponse(savedBooking, showSeats, payment);
    }

    @Transactional
    public BookingDTO.BookingResponse confirmBooking(BookingDTO.PaymentVerificationRequest request) {
        boolean paymentSuccess = paymentService.verifyAndUpdatePayment(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!paymentSuccess) {
            booking.setStatus(Booking.BookingStatus.FAILED);
            bookingRepository.save(booking);
            // Release seats
            showSeatRepository.releaseSeatsByBookingId(booking.getId());
            showRepository.increaseAvailableSeats(booking.getShow().getId(),
                    booking.getShowSeats().size());
            throw new BookingException("Payment verification failed");
        }

        // Update seat status to BOOKED
        List<Long> seatIds = booking.getShowSeats().stream()
                .map(ss -> ss.getSeat().getId())
                .collect(Collectors.toList());
        showSeatRepository.updateSeatStatus(booking.getShow().getId(), seatIds,
                ShowSeat.SeatStatus.BOOKED);

        List<ShowSeat> showSeats = showSeatRepository.findByBookingId(booking.getId());
        return buildBookingResponse(booking, showSeats, booking.getPayment());
    }

    @Transactional
    public void cancelBooking(Long bookingId, Long userId, String reason) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getUser().getId().equals(userId)) {
            throw new BookingException("Unauthorized to cancel this booking");
        }

        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new BookingException("Only confirmed bookings can be cancelled");
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancellationReason(reason);
        bookingRepository.save(booking);

        // Release seats
        showSeatRepository.releaseSeatsByBookingId(bookingId);
        showRepository.increaseAvailableSeats(booking.getShow().getId(),
                booking.getShowSeats().size());

        // Process refund
        paymentService.processRefund(bookingId);
    }

    public List<BookingDTO.BookingResponse> getUserBookings(Long userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(b -> buildBookingResponse(b, showSeatRepository.findByBookingId(b.getId()), b.getPayment()))
                .collect(Collectors.toList());
    }

    public BookingDTO.BookingResponse getBookingByReference(String reference) {
        Booking booking = bookingRepository.findByBookingReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + reference));
        List<ShowSeat> showSeats = showSeatRepository.findByBookingId(booking.getId());
        return buildBookingResponse(booking, showSeats, booking.getPayment());
    }

    private BookingDTO.BookingResponse buildBookingResponse(Booking booking, List<ShowSeat> showSeats, Payment payment) {
        BookingDTO.BookingResponse response = new BookingDTO.BookingResponse();
        response.setId(booking.getId());
        response.setBookingReference(booking.getBookingReference());
        response.setMovieTitle(booking.getShow().getMovie().getTitle());
        response.setTheatreName(booking.getShow().getTheatre().getName());
        response.setShowDate(booking.getShow().getShowDate().toString());
        response.setShowTime(booking.getShow().getStartTime().toString());
        response.setSeats(showSeats.stream()
                .map(ss -> ss.getSeat().getRowNumber() + ss.getSeat().getSeatNumber())
                .collect(Collectors.toList()));
        response.setTotalAmount(booking.getTotalAmount());
        response.setConvenienceFee(booking.getConvenienceFee());
        response.setFinalAmount(booking.getFinalAmount());
        response.setStatus(booking.getStatus());
        response.setBookedAt(booking.getBookedAt());
        if (payment != null) {
            response.setRazorpayOrderId(payment.getRazorpayOrderId());
            response.setRazorpayKeyId(paymentService.getRazorpayKeyId());
        }
        return response;
    }
}
