package org.satvik.moviebookingsystembackend.controller;


import org.satvik.moviebookingsystembackend.dto.MovieDTO;
import org.satvik.moviebookingsystembackend.service.ShowService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
public class ShowController {

    private final ShowService showService;

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<MovieDTO.ShowResponse>> getShowsByMovie(
            @PathVariable Long movieId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (city != null) {
            return ResponseEntity.ok(showService.getShowsByMovieAndCity(movieId, city));
        }
        return ResponseEntity.ok(showService.getShowsByMovieAndDate(movieId,
                date != null ? date : LocalDate.now()));
    }

    @GetMapping("/theatre/{theatreId}")
    public ResponseEntity<List<MovieDTO.ShowResponse>> getShowsByTheatre(
            @PathVariable Long theatreId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(showService.getShowsByTheatreAndDate(theatreId,
                date != null ? date : LocalDate.now()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieDTO.ShowResponse> getShow(@PathVariable Long id) {
        return ResponseEntity.ok(showService.getShowById(id));
    }

    @GetMapping("/{showId}/seats")
    public ResponseEntity<List<MovieDTO.SeatLayoutResponse>> getSeatLayout(@PathVariable Long showId) {
        return ResponseEntity.ok(showService.getSeatLayout(showId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieDTO.ShowResponse> createShow(
            @RequestBody MovieDTO.ShowRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(showService.createShow(request));
    }
}

