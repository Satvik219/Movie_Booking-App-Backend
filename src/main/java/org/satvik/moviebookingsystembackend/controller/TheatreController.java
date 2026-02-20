package org.satvik.moviebookingsystembackend.controller;


import org.satvik.moviebookingsystembackend.entity.Screen;
import org.satvik.moviebookingsystembackend.entity.Theatre;
import org.satvik.moviebookingsystembackend.repository.ScreenRepository;
import org.satvik.moviebookingsystembackend.repository.TheatreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/theatres")
@RequiredArgsConstructor
public class TheatreController {

    private final TheatreRepository theatreRepository;
    private final ScreenRepository screenRepository;

    @GetMapping
    public ResponseEntity<List<Theatre>> getAllTheatres() {
        return ResponseEntity.ok(theatreRepository.findAllActiveTheatres());
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<Theatre>> getTheatresByCity(@PathVariable String city) {
        return ResponseEntity.ok(theatreRepository.findByCity(city));
    }

    @GetMapping("/cities")
    public ResponseEntity<List<String>> getAllCities() {
        return ResponseEntity.ok(theatreRepository.findAllActiveCities());
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<Theatre>> getTheatresByMovie(
            @PathVariable Long movieId,
            @RequestParam(required = false) String city) {
        if (city != null) {
            return ResponseEntity.ok(theatreRepository.findTheatresByMovieAndCity(movieId, city));
        }
        return ResponseEntity.ok(theatreRepository.findTheatresByMovie(movieId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Theatre> getTheatreById(@PathVariable Long id) {
        return ResponseEntity.of(theatreRepository.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Theatre> createTheatre(@RequestBody Theatre theatre) {

        Theatre savedTheatre = theatreRepository.save(theatre);

        // Automatically create 5 screens for this theatre
        for (int i = 1; i <= 5; i++) {

            Screen screen = Screen.builder()
                    .name("Screen " + i)
                    .type(Screen.ScreenType.STANDARD)
                    .totalSeats(120)
                    .theatre(savedTheatre)
                    .build();

            screenRepository.save(screen);
        }

        return ResponseEntity.ok(savedTheatre);
    }
}
