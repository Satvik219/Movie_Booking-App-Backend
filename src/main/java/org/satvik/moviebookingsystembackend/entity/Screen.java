package org.satvik.moviebookingsystembackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "screens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Screen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "total_seats")
    private Integer totalSeats;

    @Enumerated(EnumType.STRING)
    private ScreenType type;

    // @JsonIgnore breaks the circular: Screen -> theatre -> Theatre -> screens -> Screen (STOP)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theatre_id", nullable = false)
    @JsonIgnore
    private Theatre theatre;

    // Not needed in API responses — keep ignored
    @OneToMany(mappedBy = "screen", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<Seat> seats = new ArrayList<>();

    @OneToMany(mappedBy = "screen", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<Show> shows = new ArrayList<>();

    public enum ScreenType {
        STANDARD, IMAX, FOUR_DX, GOLD_CLASS
    }
}