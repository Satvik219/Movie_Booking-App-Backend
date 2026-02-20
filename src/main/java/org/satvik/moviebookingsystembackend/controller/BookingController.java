package org.satvik.moviebookingsystembackend.controller;

import org.satvik.moviebookingsystembackend.dto.BookingDTO;
import org.satvik.moviebookingsystembackend.entity.User;
import org.satvik.moviebookingsystembackend.service.BookingService;
import org.satvik.moviebookingsystembackend.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final TicketService ticketService;

    @PostMapping("/initiate")
    public ResponseEntity<BookingDTO.BookingResponse> initiateBooking(
            @AuthenticationPrincipal User user,
            @RequestBody BookingDTO.BookingRequest request) {
        return ResponseEntity.ok(bookingService.initiateBooking(user.getId(), request));
    }

    @PostMapping("/confirm")
    public ResponseEntity<BookingDTO.BookingResponse> confirmBooking(
            @RequestBody BookingDTO.PaymentVerificationRequest request) {
        return ResponseEntity.ok(bookingService.confirmBooking(request));
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal User user,
            @RequestBody BookingDTO.CancelBookingRequest request) {
        bookingService.cancelBooking(bookingId, user.getId(), request.getReason());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<List<BookingDTO.BookingResponse>> getMyBookings(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(bookingService.getUserBookings(user.getId()));
    }

    @GetMapping("/reference/{reference}")
    public ResponseEntity<BookingDTO.BookingResponse> getBookingByReference(
            @PathVariable String reference) {
        return ResponseEntity.ok(bookingService.getBookingByReference(reference));
    }

    @GetMapping("/{bookingId}/ticket")
    public ResponseEntity<byte[]> downloadTicket(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal User user) {
        byte[] ticketPdf = ticketService.generateTicketPdf(bookingId, user.getId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ticket-" + bookingId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(ticketPdf);
    }
}

