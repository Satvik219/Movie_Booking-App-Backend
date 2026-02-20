package org.satvik.moviebookingsystembackend.service;



import org.satvik.moviebookingsystembackend.dto.MovieDTO;
import org.satvik.moviebookingsystembackend.entity.*;
import org.satvik.moviebookingsystembackend.exception.ResourceNotFoundException;
import org.satvik.moviebookingsystembackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShowService {

    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final TheatreRepository theatreRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final ShowSeatRepository showSeatRepository;

    public List<MovieDTO.ShowResponse> getShowsByMovieAndDate(Long movieId, LocalDate date) {
        return showRepository.findByMovieAndDate(movieId, date).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<MovieDTO.ShowResponse> getShowsByMovieAndCity(Long movieId, String city) {
        return showRepository.findByMovieAndCityFromDate(movieId, city, LocalDate.now()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<MovieDTO.ShowResponse> getShowsByTheatreAndDate(Long theatreId, LocalDate date) {
        return showRepository.findByTheatreAndDate(theatreId, date).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public MovieDTO.ShowResponse getShowById(Long id) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with id: " + id));
        return mapToResponse(show);
    }

    public List<MovieDTO.SeatLayoutResponse> getSeatLayout(Long showId) {
        return showSeatRepository.findByShowId(showId).stream()
                .map(ss -> {
                    MovieDTO.SeatLayoutResponse resp = new MovieDTO.SeatLayoutResponse();
                    resp.setShowSeatId(ss.getId());
                    resp.setSeatId(ss.getSeat().getId());
                    resp.setSeatNumber(ss.getSeat().getSeatNumber());
                    resp.setRowNumber(ss.getSeat().getRowNumber());
                    resp.setSeatType(ss.getSeat().getSeatType().name());
                    resp.setStatus(ss.getStatus().name());
                    resp.setPrice(ss.getPrice());
                    return resp;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public MovieDTO.ShowResponse createShow(MovieDTO.ShowRequest request) {
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found"));
        Theatre theatre = theatreRepository.findById(request.getTheatreId())
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found"));
        Screen screen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found"));

        List<Seat> seats = seatRepository.findByScreenId(screen.getId());

        Show show = Show.builder()
                .movie(movie)
                .theatre(theatre)
                .screen(screen)
                .showDate(request.getShowDate())
                .startTime(request.getStartTime())
                .endTime(request.getStartTime().plusMinutes(movie.getDurationMinutes() + 15))
                .silverPrice(request.getSilverPrice())
                .goldPrice(request.getGoldPrice())
                .platinumPrice(request.getPlatinumPrice())
                .reclinerPrice(request.getReclinerPrice())
                .availableSeats(seats.size())
                .status(Show.ShowStatus.UPCOMING)
                .build();

        Show savedShow = showRepository.save(show);

        // Create ShowSeat entries
        List<ShowSeat> showSeats = new ArrayList<>();
        for (Seat seat : seats) {
            Double price = getPriceForSeatType(seat.getSeatType(), request);
            ShowSeat showSeat = ShowSeat.builder()
                    .show(savedShow)
                    .seat(seat)
                    .status(ShowSeat.SeatStatus.AVAILABLE)
                    .price(price)
                    .build();
            showSeats.add(showSeat);
        }
        showSeatRepository.saveAll(showSeats);

        return mapToResponse(savedShow);
    }

    private Double getPriceForSeatType(Seat.SeatType type, MovieDTO.ShowRequest request) {
        return switch (type) {
            case SILVER -> request.getSilverPrice();
            case GOLD -> request.getGoldPrice();
            case PLATINUM -> request.getPlatinumPrice();
            case RECLINER -> request.getReclinerPrice();
        };
    }

    private MovieDTO.ShowResponse mapToResponse(Show show) {
        MovieDTO.ShowResponse response = new MovieDTO.ShowResponse();
        response.setId(show.getId());
        response.setMovieId(show.getMovie().getId());
        response.setMovieTitle(show.getMovie().getTitle());
        response.setMoviePoster(show.getMovie().getPosterUrl());
        response.setTheatreId(show.getTheatre().getId());
        response.setTheatreName(show.getTheatre().getName());
        response.setTheatreCity(show.getTheatre().getCity());
        response.setScreenId(show.getScreen().getId());
        response.setScreenName(show.getScreen().getName());
        response.setScreenType(show.getScreen().getType() != null ? show.getScreen().getType().name() : "STANDARD");
        response.setShowDate(show.getShowDate());
        response.setStartTime(show.getStartTime());
        response.setEndTime(show.getEndTime());
        response.setSilverPrice(show.getSilverPrice());
        response.setGoldPrice(show.getGoldPrice());
        response.setPlatinumPrice(show.getPlatinumPrice());
        response.setReclinerPrice(show.getReclinerPrice());
        response.setStatus(show.getStatus());
        response.setAvailableSeats(show.getAvailableSeats());
        return response;
    }
}

