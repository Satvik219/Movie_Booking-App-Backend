package org.satvik.moviebookingsystembackend.dto;

import org.satvik.moviebookingsystembackend.entity.Movie;
import org.satvik.moviebookingsystembackend.entity.Show;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class MovieDTO {

    @Data
    public static class MovieRequest {
        private String title;
        private String description;
        private String genre;
        private String language;
        private String director;
        private String cast;
        private Integer durationMinutes;
        private LocalDate releaseDate;
        private String posterUrl;
        private String trailerUrl;
        private Double rating;
        private Movie.Certificate certificate;
    }

    @Data
    public static class MovieResponse {
        private Long id;
        private String title;
        private String description;
        private String genre;
        private String language;
        private String director;
        private String cast;
        private Integer durationMinutes;
        private LocalDate releaseDate;
        private String posterUrl;
        private String trailerUrl;
        private Double rating;
        private Movie.Certificate certificate;
        private boolean active;
    }

    @Data
    public static class ShowRequest {
        private Long movieId;
        private Long theatreId;
        private Long screenId;
        private LocalDate showDate;
        private LocalTime startTime;
        private Double silverPrice;
        private Double goldPrice;
        private Double platinumPrice;
        private Double reclinerPrice;
    }

    @Data
    public static class ShowResponse {
        private Long id;
        private Long movieId;
        private String movieTitle;
        private String moviePoster;
        private Long theatreId;
        private String theatreName;
        private String theatreCity;
        private Long screenId;
        private String screenName;
        private String screenType;
        private LocalDate showDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private Double silverPrice;
        private Double goldPrice;
        private Double platinumPrice;
        private Double reclinerPrice;
        private Show.ShowStatus status;
        private Integer availableSeats;
    }

    @Data
    public static class SeatLayoutResponse {
        private Long showSeatId;
        private Long seatId;
        private String seatNumber;
        private String rowNumber;
        private String seatType;
        private String status;
        private Double price;
    }
}

