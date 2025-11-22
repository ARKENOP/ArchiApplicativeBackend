package epsi.archiapp.backend.repository;

import epsi.archiapp.backend.model.Spectacle;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SpectacleRepository extends JpaRepository<Spectacle, Long> {

    /**
     * Récupère un spectacle avec un verrouillage pessimiste en écriture.
     * Utilisé lors des réservations pour éviter les race conditions (overbooking).
     * Le verrou est libéré à la fin de la transaction.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Spectacle s WHERE s.id = :id")
    Optional<Spectacle> findByIdWithLock(@Param("id") Long id);

    Spectacle findByTitle(String title);

    @Query("SELECT s FROM Spectacle s WHERE s.date >= :now ORDER BY s.date ASC")
    Page<Spectacle> findUpcomingSpectacles(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT s FROM Spectacle s WHERE s.availableTickets > 0 AND s.date >= :now")
    List<Spectacle> findAvailableSpectacles(@Param("now") LocalDateTime now);

    @Query("SELECT s FROM Spectacle s WHERE LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Spectacle> searchSpectacles(@Param("keyword") String keyword, Pageable pageable);
}

