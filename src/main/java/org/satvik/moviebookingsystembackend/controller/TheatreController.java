package org.satvik.moviebookingsystembackend.controller;


import org.satvik.moviebookingsystembackend.dto.TheatreRequest;
import org.satvik.moviebookingsystembackend.entity.Screen;
import org.satvik.moviebookingsystembackend.entity.Seat;
import org.satvik.moviebookingsystembackend.entity.Theatre;
import org.satvik.moviebookingsystembackend.repository.ScreenRepository;
import org.satvik.moviebookingsystembackend.repository.SeatRepository;
import org.satvik.moviebookingsystembackend.repository.TheatreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/theatres")
@RequiredArgsConstructor
public class TheatreController {

    private final TheatreRepository theatreRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;

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
    @Transactional
    public ResponseEntity<Theatre> createTheatre(@RequestBody TheatreRequest request) {
        Theatre theatre = Theatre.builder()
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .pinCode(request.getPinCode())
                .phone(request.getPhone())
                .active(true)
                .build();

        Theatre savedTheatre = theatreRepository.save(theatre);

        if (request.getScreens() != null) {
            for (TheatreRequest.ScreenRequest sr : request.getScreens()) {
                Screen.ScreenType screenType;
                try { screenType = Screen.ScreenType.valueOf(sr.getType()); }
                catch (Exception e) { screenType = Screen.ScreenType.STANDARD; }

                Screen screen = Screen.builder()
                        .name(sr.getName())
                        .type(screenType)
                        .totalSeats(sr.getTotalSeats())
                        .theatre(savedTheatre)
                        .build();
                Screen savedScreen = screenRepository.save(screen);

                // Generate seats
                generateSeats(savedScreen, sr);
            }
        }

        return ResponseEntity.ok(
                theatreRepository.findById(savedTheatre.getId()).orElse(savedTheatre)
        );
    }

    private void generateSeats(Screen screen, TheatreRequest.ScreenRequest sr) {
        List<Seat> seats = new ArrayList<>();
        String[] rows = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".split("");

        // Map seat counts by type
        int[][] seatGroups = {
                { sr.getSilverSeats() != null ? sr.getSilverSeats() : 0, 0 },   // type index 0 = SILVER
                { sr.getGoldSeats() != null ? sr.getGoldSeats() : 0, 1 },       // 1 = GOLD
                { sr.getPlatinumSeats() != null ? sr.getPlatinumSeats() : 0, 2 }, // 2 = PLATINUM
                { sr.getReclinerSeats() != null ? sr.getReclinerSeats() : 0, 3 }, // 3 = RECLINER
        };
        Seat.SeatType[] types = { Seat.SeatType.SILVER, Seat.SeatType.GOLD,
                Seat.SeatType.PLATINUM, Seat.SeatType.RECLINER };

        int rowIdx = 0;
        for (int[] group : seatGroups) {
            int count = group[0];
            Seat.SeatType type = types[group[1]];
            if (count <= 0) continue;

            int seatsPerRow = 20; // or any number you prefer
            int seatsLeft = count;
            while (seatsLeft > 0 && rowIdx < rows.length) {
                int inThisRow = Math.min(seatsLeft, seatsPerRow);
                String row = rows[rowIdx++];
                for (int seatNum = 1; seatNum <= inThisRow; seatNum++) {
                    seats.add(Seat.builder()
                            .rowNumber(row)
                            .seatNumber(String.valueOf(seatNum))
                            .seatType(type)
                            .screen(screen)
                            .build());
                }
                seatsLeft -= inThisRow;
            }
        }
        seatRepository.saveAll(seats);
    }
}
