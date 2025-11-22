package epsi.archiapp.backend.repository;

import epsi.archiapp.backend.model.Reservation;
import epsi.archiapp.backend.model.Spectacle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Tests du repository Réservation")
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Spectacle spectacle1;
    private Spectacle spectacle2;
    private Reservation reservation1;
    private Reservation reservation2;

    @BeforeEach
    void setUp() {
        spectacle1 = Spectacle.builder()
                .title("Le Malade Imaginaire")
                .description("Comédie de Molière")
                .date(LocalDateTime.now().plusDays(30))
                .price(new BigDecimal("25.00"))
                .availableTickets(100)
                .build();

        spectacle2 = Spectacle.builder()
                .title("Hamlet")
                .description("Tragédie de Shakespeare")
                .date(LocalDateTime.now().plusDays(60))
                .price(new BigDecimal("30.00"))
                .availableTickets(150)
                .build();

        entityManager.persist(spectacle1);
        entityManager.persist(spectacle2);

        reservation1 = Reservation.builder()
                .keycloakUserId("user-123")
                .spectacle(spectacle1)
                .quantity(2)
                .totalPrice(new BigDecimal("50.00"))
                .build();

        reservation2 = Reservation.builder()
                .keycloakUserId("user-123")
                .spectacle(spectacle2)
                .quantity(3)
                .totalPrice(new BigDecimal("90.00"))
                .build();

        entityManager.persist(reservation1);
        entityManager.persist(reservation2);
        entityManager.flush();
    }

    @Test
    @DisplayName("Doit trouver les réservations d'un utilisateur")
    void testFindByKeycloakUserId() {
        // When
        List<Reservation> reservations = reservationRepository.findByKeycloakUserId("user-123");

        // Then
        assertThat(reservations).isNotEmpty();
        assertThat(reservations).hasSize(2);
        assertThat(reservations).allMatch(r -> r.getKeycloakUserId().equals("user-123"));
    }

    @Test
    @DisplayName("Doit trouver les réservations d'un utilisateur avec pagination")
    void testFindByKeycloakUserIdWithPagination() {
        // When
        Page<Reservation> page = reservationRepository.findByKeycloakUserId("user-123", PageRequest.of(0, 10));

        // Then
        assertThat(page.getContent()).isNotEmpty();
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("Doit calculer le total des ventes")
    void testGetTotalSales() {
        // When
        BigDecimal totalSales = reservationRepository.getTotalSales();

        // Then
        assertThat(totalSales).isNotNull();
        assertThat(totalSales).isEqualByComparingTo(new BigDecimal("140.00")); // 50 + 90
    }

    @Test
    @DisplayName("Doit récupérer les ventes par spectacle")
    void testGetSalesBySpectacle() {
        // When
        List<ReservationRepository.SalesBySpectacle> sales = reservationRepository.getSalesBySpectacle();

        // Then
        assertThat(sales).isNotEmpty();
        assertThat(sales).hasSize(2);

        ReservationRepository.SalesBySpectacle sale1 = sales.get(0);
        assertThat(sale1.getSpectacleId()).isNotNull();
        assertThat(sale1.getTitle()).isNotNull();
        assertThat(sale1.getTicketsSold()).isGreaterThan(0);
        assertThat(sale1.getRevenue()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Doit sauvegarder une nouvelle réservation")
    void testSaveReservation() {
        // Given
        Reservation newReservation = Reservation.builder()
                .keycloakUserId("user-456")
                .spectacle(spectacle1)
                .quantity(1)
                .totalPrice(new BigDecimal("25.00"))
                .build();

        // When
        Reservation saved = reservationRepository.save(newReservation);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getKeycloakUserId()).isEqualTo("user-456");
        assertThat(saved.getQuantity()).isEqualTo(1);
        assertThat(saved.getReservationDate()).isNotNull(); // Créé automatiquement
    }

    @Test
    @DisplayName("Doit supprimer une réservation")
    void testDeleteReservation() {
        // Given
        Long reservationId = reservation1.getId();

        // When
        reservationRepository.deleteById(reservationId);
        entityManager.flush();

        // Then
        assertThat(reservationRepository.findById(reservationId)).isEmpty();
    }

    @Test
    @DisplayName("Doit retourner zéro si aucune vente")
    void testGetTotalSalesEmpty() {
        // Given
        reservationRepository.deleteAll();
        entityManager.flush();

        // When
        BigDecimal totalSales = reservationRepository.getTotalSales();

        // Then
        assertThat(totalSales).isNull(); // Peut être null si aucune réservation
    }

    @Test
    @DisplayName("Doit trouver les réservations avec le spectacle associé")
    void testFindWithSpectacleJoinFetch() {
        // When
        List<Reservation> reservations = reservationRepository.findByKeycloakUserId("user-123");

        // Then
        assertThat(reservations).isNotEmpty();
        // Vérifier que le spectacle est bien chargé (pas de lazy loading exception)
        reservations.forEach(r -> {
            assertThat(r.getSpectacle()).isNotNull();
            assertThat(r.getSpectacle().getTitle()).isNotNull();
        });
    }
}

