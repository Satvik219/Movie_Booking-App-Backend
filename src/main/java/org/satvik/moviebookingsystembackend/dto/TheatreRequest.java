package org.satvik.moviebookingsystembackend.dto;

import lombok.Data;
import java.util.List;

@Data
public class TheatreRequest {
    private String name;
    private String address;
    private String city;
    private String state;
    private String pinCode;
    private String phone;
    private boolean active = true;
    private List<ScreenRequest> screens;

    @Data
    public static class ScreenRequest {
        private String name;
        private String type; // STANDARD, IMAX, FOUR_DX, GOLD_CLASS
        private Integer totalSeats;
        private Integer silverSeats;
        private Integer goldSeats;
        private Integer platinumSeats;
        private Integer reclinerSeats;
    }
}
