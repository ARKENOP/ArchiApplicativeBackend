package epsi.archiapp.backend.repository;

import epsi.archiapp.backend.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r JOIN FETCH r.spectacle WHERE r.keycloakUserId = :keycloakUserId ORDER BY r.reservationDate DESC")
    List<Reservation> findByKeycloakUserId(@Param("keycloakUserId") String keycloakUserId);

    @Query("SELECT SUM(r.totalPrice) FROM Reservation r")
    java.math.BigDecimal getTotalSales();

    interface SalesBySpectacle {
        Long getSpectacleId();
        String getTitle();
        Long getTicketsSold();
        java.math.BigDecimal getRevenue();
    }

    @Query("SELECT r.spectacle.id as spectacleId, r.spectacle.title as title, " +
           "SUM(r.quantity) as ticketsSold, SUM(r.totalPrice) as revenue " +
           "FROM Reservation r " +
           "GROUP BY r.spectacle.id, r.spectacle.title " +
           "ORDER BY revenue DESC")
    List<SalesBySpectacle> getSalesBySpectacle();
}
