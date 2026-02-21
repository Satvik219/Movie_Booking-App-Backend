package org.satvik.moviebookingsystembackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "theatres")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Theatre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    private String state;

    @Column(name = "pin_code")
    private String pinCode;

    private String phone;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    // LAZY is fine — repository uses LEFT JOIN FETCH t.screens
    // @JsonIgnoreProperties on screens breaks the circular:
    //   Theatre -> screens -> Screen.theatre -> back to Theatre (STOP)
    @OneToMany(mappedBy = "theatre", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"theatre", "seats", "shows"})
    @Builder.Default
    private List<Screen> screens = new ArrayList<>();

    // shows excluded from JSON entirely to avoid further circular refs
    @OneToMany(mappedBy = "theatre", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<Show> shows = new ArrayList<>();
}