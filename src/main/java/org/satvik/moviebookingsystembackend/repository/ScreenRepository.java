package org.satvik.moviebookingsystembackend.repository;

import org.satvik.moviebookingsystembackend.entity.Screen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScreenRepository extends JpaRepository<Screen, Long> {

    @Query("SELECT sc FROM Screen sc WHERE sc.theatre.id = :theatreId ORDER BY sc.name")
    List<Screen> findByTheatreId(@Param("theatreId") Long theatreId);

    @Query("SELECT sc FROM Screen sc WHERE sc.theatre.id = :theatreId AND sc.type = :type")
    List<Screen> findByTheatreIdAndType(@Param("theatreId") Long theatreId, @Param("type") Screen.ScreenType type);

    @Query("SELECT COUNT(sc) FROM Screen sc WHERE sc.theatre.id = :theatreId")
    Long countByTheatreId(@Param("theatreId") Long theatreId);
}

