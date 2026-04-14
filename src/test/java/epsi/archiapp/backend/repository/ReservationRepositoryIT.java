package epsi.archiapp.backend.repository;

import epsi.archiapp.backend.model.Reservation;
import epsi.archiapp.backend.model.Spectacle;
import epsi.archiapp.backend.testsupport.BasePostgresContainerIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReservationRepositoryIT extends BasePostgresContainerIT {

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private SpectacleRepository spectacleRepository;

    @BeforeEach
    void clean() {
        reservationRepository.deleteAll();
        spectacleRepository.deleteAll();
    }

    @Test
    void findByKeycloakUserId_shouldReturnPagedReservationsForUser() {
        Spectacle spectacle = saveSpectacle("Hamlet", new BigDecimal("30.00"));
        saveReservation("user-a", 2, spectacle, new BigDecimal("60.00"));
        saveReservation("user-a", 1, spectacle, new BigDecimal("30.00"));
        saveReservation("user-b", 4, spectacle, new BigDecimal("120.00"));

        var result = reservationRepository.findByKeycloakUserId("user-a", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).allMatch(r -> "user-a".equals(r.getKeycloakUserId()));
    }

    @Test
    void getTotalSales_shouldSumAllReservationAmounts() {
        Spectacle spectacle = saveSpectacle("Le Cid", new BigDecimal("20.00"));
        saveReservation("user-a", 1, spectacle, new BigDecimal("20.00"));
        saveReservation("user-b", 2, spectacle, new BigDecimal("40.00"));

        BigDecimal total = reservationRepository.getTotalSales();

        assertThat(total).isEqualByComparingTo("60.00");
    }

    @Test
    void getSalesBySpectacle_shouldAggregateBySpectacleAndSortByRevenue() {
        Spectacle hamlet = saveSpectacle("Hamlet", new BigDecimal("30.00"));
        Spectacle cid = saveSpectacle("Le Cid", new BigDecimal("20.00"));

        saveReservation("u1", 3, hamlet, new BigDecimal("90.00"));
        saveReservation("u2", 1, hamlet, new BigDecimal("30.00"));
        saveReservation("u3", 2, cid, new BigDecimal("40.00"));

        List<ReservationRepository.SalesBySpectacle> rows = reservationRepository.getSalesBySpectacle();

        assertThat(rows).hasSize(2);
        assertThat(rows.getFirst().getTitle()).isEqualTo("Hamlet");
        assertThat(rows.getFirst().getTicketsSold()).isEqualTo(4L);
        assertThat(rows.getFirst().getRevenue()).isEqualByComparingTo("120.00");
    }

    @Test
    void deleteBySpectacleId_shouldDeleteOnlyTargetReservations() {
        Spectacle hamlet = saveSpectacle("Hamlet", new BigDecimal("30.00"));
        Spectacle cid = saveSpectacle("Le Cid", new BigDecimal("20.00"));

        Reservation hamletReservation = saveReservation("u1", 1, hamlet, new BigDecimal("30.00"));
        saveReservation("u2", 1, cid, new BigDecimal("20.00"));

        reservationRepository.deleteBySpectacleId(hamlet.getId());

        assertThat(reservationRepository.findById(hamletReservation.getId())).isEmpty();
        assertThat(reservationRepository.count()).isEqualTo(1);
    }

    private Spectacle saveSpectacle(String title, BigDecimal price) {
        Spectacle spectacle = Spectacle.builder()
                .title(title)
                .description("desc")
                .date(LocalDateTime.now().plusDays(3))
                .price(price)
                .availableTickets(100)
                .imageUrl("https://example.test/img.png")
                .createdBy("test")
                .createdAt(LocalDateTime.now())
                .build();

        return spectacleRepository.save(spectacle);
    }

    private Reservation saveReservation(String userId, int quantity, Spectacle spectacle, BigDecimal totalPrice) {
        Reservation reservation = Reservation.builder()
                .keycloakUserId(userId)
                .quantity(quantity)
                .totalPrice(totalPrice)
                .reservationDate(LocalDateTime.now())
                .spectacle(spectacle)
                .build();

        return reservationRepository.save(reservation);
    }
}


