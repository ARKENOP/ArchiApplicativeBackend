package epsi.archiapp.backend.repository;

import epsi.archiapp.backend.model.Reservation;
import epsi.archiapp.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @Query("select r from Reservation r join fetch r.spectacle where r.user = :user")
    List<Reservation> findByUser(@Param("user") User user);

    @Query("select sum(r.totalPrice) from Reservation r")
    java.math.BigDecimal totalSales();

    interface SalesBySpectacle {
        Long getSpectacleId();
        String getTitle();
        Long getTicketsSold();
        java.math.BigDecimal getRevenue();
    }

    @Query("select r.spectacle.id as spectacleId, r.spectacle.title as title, sum(r.quantity) as ticketsSold, sum(r.totalPrice) as revenue from Reservation r group by r.spectacle.id, r.spectacle.title order by revenue desc")
    List<SalesBySpectacle> salesBySpectacle();
}
