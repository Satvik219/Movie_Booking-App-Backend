package org.satvik.moviebookingsystembackend.service;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import org.satvik.moviebookingsystembackend.entity.Booking;
import org.satvik.moviebookingsystembackend.entity.ShowSeat;
import org.satvik.moviebookingsystembackend.exception.BookingException;
import org.satvik.moviebookingsystembackend.exception.ResourceNotFoundException;
import org.satvik.moviebookingsystembackend.repository.BookingRepository;
import org.satvik.moviebookingsystembackend.repository.ShowSeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final BookingRepository bookingRepository;
    private final ShowSeatRepository showSeatRepository;

    public byte[] generateTicketPdf(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getUser().getId().equals(userId)) {
            throw new BookingException("Unauthorized to download this ticket");
        }

        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new BookingException("Ticket only available for confirmed bookings");
        }

        List<ShowSeat> showSeats = showSeatRepository.findByBookingId(bookingId);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();

            // Header
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, BaseColor.DARK_GRAY);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Font labelFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, new BaseColor(100, 100, 100));
            Font valueFont = new Font(Font.FontFamily.HELVETICA, 12);

            // Logo/Header
            Paragraph title = new Paragraph("🎬 MOVIE TICKET", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5);
            document.add(title);

            Paragraph subtitle = new Paragraph("Movie Booking System", new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC, BaseColor.GRAY));
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            // Divider
            LineSeparator ls = new LineSeparator();
            ls.setLineColor(new BaseColor(220, 220, 220));
            document.add(new Chunk(ls));

            // Booking Reference
            Paragraph ref = new Paragraph();
            ref.setSpacingBefore(15);
            ref.add(new Chunk("Booking Reference: ", labelFont));
            ref.add(new Chunk(booking.getBookingReference(), new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, new BaseColor(59, 130, 246))));
            document.add(ref);

            // Movie Details Table
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(15);
            table.setSpacingAfter(15);
            table.setWidths(new float[]{1f, 2f});

            addTableRow(table, "Movie", booking.getShow().getMovie().getTitle(), labelFont, valueFont);
            addTableRow(table, "Theatre", booking.getShow().getTheatre().getName(), labelFont, valueFont);
            addTableRow(table, "City", booking.getShow().getTheatre().getCity(), labelFont, valueFont);
            addTableRow(table, "Screen", booking.getShow().getScreen().getName(), labelFont, valueFont);
            addTableRow(table, "Date", booking.getShow().getShowDate().toString(), labelFont, valueFont);
            addTableRow(table, "Time", booking.getShow().getStartTime().toString(), labelFont, valueFont);
            addTableRow(table, "Seats", showSeats.stream()
                    .map(ss -> ss.getSeat().getRowNumber() + ss.getSeat().getSeatNumber())
                    .collect(Collectors.joining(", ")), labelFont, valueFont);
            addTableRow(table, "No. of Seats", String.valueOf(showSeats.size()), labelFont, valueFont);
            document.add(table);

            // Payment Summary
            document.add(new Chunk(ls));
            Paragraph paymentHeader = new Paragraph("PAYMENT SUMMARY", headerFont);
            paymentHeader.setSpacingBefore(10);
            document.add(paymentHeader);

            PdfPTable payTable = new PdfPTable(2);
            payTable.setWidthPercentage(60);
            payTable.setHorizontalAlignment(Element.ALIGN_LEFT);
            payTable.setSpacingBefore(10);
            payTable.setWidths(new float[]{1.5f, 1f});

            addPaymentRow(payTable, "Subtotal", "₹" + booking.getTotalAmount(), labelFont, valueFont);
            addPaymentRow(payTable, "Convenience Fee", "₹" + booking.getConvenienceFee(), labelFont, valueFont);
            addPaymentRow(payTable, "Total Paid", "₹" + booking.getFinalAmount(),
                    new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD),
                    new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, new BaseColor(22, 163, 74)));
            document.add(payTable);

            // Footer
            document.add(new Chunk(ls));
            Paragraph footer = new Paragraph("Thank you for booking with us! Enjoy your movie! 🍿",
                    new Font(Font.FontFamily.HELVETICA, 11, Font.ITALIC, BaseColor.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(20);
            document.add(footer);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating ticket PDF: {}", e.getMessage());
            throw new RuntimeException("Failed to generate ticket: " + e.getMessage());
        }
    }

    private void addTableRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.BOTTOM);
        labelCell.setPadding(8);
        labelCell.setBackgroundColor(new BaseColor(248, 250, 252));

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.BOTTOM);
        valueCell.setPadding(8);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addPaymentRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(5);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}

