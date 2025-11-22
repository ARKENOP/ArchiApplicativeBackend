package epsi.archiapp.backend.mapper;

import epsi.archiapp.backend.dto.ReservationRequest;
import epsi.archiapp.backend.dto.ReservationResponse;
import epsi.archiapp.backend.model.Reservation;
import epsi.archiapp.backend.model.Spectacle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests du mapper Réservation")
class ReservationMapperTest {

    private ReservationMapper reservationMapper;
    private Spectacle spectacle;

    @BeforeEach
    void setUp() {
        reservationMapper = new ReservationMapper();

        spectacle = Spectacle.builder()
                .id(1L)
                .title("Le Malade Imaginaire")
                .description("Comédie de Molière")
                .date(LocalDateTime.now().plusDays(30))
                .price(new BigDecimal("25.00"))
                .availableTickets(100)
                .imageUrl("https://example.com/image.jpg")
                .build();
    }

    @Test
    @DisplayName("Doit convertir une entité Reservation en ReservationResponse")
    void testToResponse() {
        // Given
        LocalDateTime reservationDate = LocalDateTime.now();
        Reservation reservation = Reservation.builder()
                .id(1L)
                .keycloakUserId("user-123")
                .spectacle(spectacle)
                .quantity(2)
                .totalPrice(new BigDecimal("50.00"))
                .reservationDate(reservationDate)
                .build();

        // When
        ReservationResponse response = reservationMapper.toResponse(reservation);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getQuantity()).isEqualTo(2);
        assertThat(response.getTotalPrice()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(response.getReservationDate()).isEqualTo(reservationDate);

        // Vérifier les informations du spectacle imbriqué
        assertThat(response.getSpectacle()).isNotNull();
        assertThat(response.getSpectacle().getId()).isEqualTo(1L);
        assertThat(response.getSpectacle().getTitle()).isEqualTo("Le Malade Imaginaire");
        assertThat(response.getSpectacle().getPrice()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(response.getSpectacle().getImageUrl()).isEqualTo("https://example.com/image.jpg");
    }

    @Test
    @DisplayName("Doit retourner null si l'entité est null")
    void testToResponseNull() {
        // When
        ReservationResponse response = reservationMapper.toResponse(null);

        // Then
        assertThat(response).isNull();
    }

    @Test
    @DisplayName("Doit convertir un ReservationRequest en entité Reservation")
    void testToEntity() {
        // Given
        ReservationRequest request = new ReservationRequest(1L, 3);
        String userId = "user-456";
        BigDecimal totalPrice = new BigDecimal("75.00");

        // When
        Reservation reservation = reservationMapper.toEntity(request, spectacle, userId, totalPrice);

        // Then
        assertThat(reservation).isNotNull();
        assertThat(reservation.getId()).isNull(); // Pas encore persisté
        assertThat(reservation.getKeycloakUserId()).isEqualTo(userId);
        assertThat(reservation.getSpectacle()).isEqualTo(spectacle);
        assertThat(reservation.getQuantity()).isEqualTo(3);
        assertThat(reservation.getTotalPrice()).isEqualByComparingTo(totalPrice);
    }

    @Test
    @DisplayName("Doit retourner null si le request ou le spectacle est null")
    void testToEntityNull() {
        // Given
        ReservationRequest request = new ReservationRequest(1L, 2);

        // When - request null
        Reservation result1 = reservationMapper.toEntity(null, spectacle, "user", BigDecimal.TEN);

        // Then
        assertThat(result1).isNull();

        // When - spectacle null
        Reservation result2 = reservationMapper.toEntity(request, null, "user", BigDecimal.TEN);

        // Then
        assertThat(result2).isNull();
    }

    @Test
    @DisplayName("Doit calculer correctement le prix total dans le mapping")
    void testTotalPriceMapping() {
        // Given
        ReservationRequest request = new ReservationRequest(1L, 5);
        BigDecimal unitPrice = spectacle.getPrice(); // 25.00
        BigDecimal expectedTotal = unitPrice.multiply(BigDecimal.valueOf(5)); // 125.00

        // When
        Reservation reservation = reservationMapper.toEntity(request, spectacle, "user", expectedTotal);

        // Then
        assertThat(reservation.getTotalPrice()).isEqualByComparingTo(expectedTotal);
        assertThat(reservation.getQuantity()).isEqualTo(5);
    }
}

