package epsi.archiapp.backend.service;

import epsi.archiapp.backend.dto.ReservationRequest;
import epsi.archiapp.backend.dto.ReservationResponse;
import epsi.archiapp.backend.exception.InsufficientTicketsException;
import epsi.archiapp.backend.exception.ResourceNotFoundException;
import epsi.archiapp.backend.exception.UnauthorizedAccessException;
import epsi.archiapp.backend.mapper.ReservationMapper;
import epsi.archiapp.backend.model.Reservation;
import epsi.archiapp.backend.model.Spectacle;
import epsi.archiapp.backend.repository.ReservationRepository;
import epsi.archiapp.backend.repository.SpectacleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du service Réservation")
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private SpectacleRepository spectacleRepository;

    @Mock
    private ReservationMapper reservationMapper;

    @InjectMocks
    private ReservationService reservationService;

    private Spectacle spectacle;
    private Reservation reservation;
    private ReservationRequest reservationRequest;
    private ReservationResponse reservationResponse;
    private final String userId = "user-123";

    @BeforeEach
    void setUp() {
        spectacle = Spectacle.builder()
                .id(1L)
                .title("Le Malade Imaginaire")
                .description("Comédie de Molière")
                .date(LocalDateTime.now().plusDays(30))
                .price(new BigDecimal("25.00"))
                .availableTickets(100)
                .version(1L)
                .build();

        reservationRequest = new ReservationRequest(1L, 2);

        reservation = Reservation.builder()
                .id(1L)
                .keycloakUserId(userId)
                .spectacle(spectacle)
                .quantity(2)
                .totalPrice(new BigDecimal("50.00"))
                .reservationDate(LocalDateTime.now())
                .build();

        reservationResponse = ReservationResponse.builder()
                .id(1L)
                .quantity(2)
                .totalPrice(new BigDecimal("50.00"))
                .reservationDate(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Doit créer une réservation avec succès")
    void testCreateReservation() {
        // Given
        when(spectacleRepository.findByIdWithLock(1L)).thenReturn(Optional.of(spectacle));
        when(reservationMapper.toEntity(eq(reservationRequest), eq(spectacle), eq(userId), any(BigDecimal.class)))
                .thenReturn(reservation);
        when(reservationRepository.save(reservation)).thenReturn(reservation);
        when(reservationMapper.toResponse(reservation)).thenReturn(reservationResponse);
        when(spectacleRepository.save(spectacle)).thenReturn(spectacle);

        // When
        ReservationResponse result = reservationService.createReservation(userId, reservationRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getQuantity()).isEqualTo(2);
        assertThat(spectacle.getAvailableTickets()).isEqualTo(98); // 100 - 2
        verify(spectacleRepository).findByIdWithLock(1L);
        verify(reservationRepository).save(reservation);
        verify(spectacleRepository).save(spectacle);
    }

    @Test
    @DisplayName("Doit lancer InsufficientTicketsException si pas assez de billets")
    void testCreateReservationInsufficientTickets() {
        // Given
        spectacle.setAvailableTickets(1);
        when(spectacleRepository.findByIdWithLock(1L)).thenReturn(Optional.of(spectacle));

        // When & Then
        assertThatThrownBy(() -> reservationService.createReservation(userId, reservationRequest))
                .isInstanceOf(InsufficientTicketsException.class);
        verify(spectacleRepository).findByIdWithLock(1L);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Doit lancer ResourceNotFoundException si le spectacle n'existe pas")
    void testCreateReservationSpectacleNotFound() {
        // Given
        when(spectacleRepository.findByIdWithLock(999L)).thenReturn(Optional.empty());
        reservationRequest.setSpectacleId(999L);

        // When & Then
        assertThatThrownBy(() -> reservationService.createReservation(userId, reservationRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Spectacle");
        verify(spectacleRepository).findByIdWithLock(999L);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Doit lancer IllegalStateException si le spectacle est passé")
    void testCreateReservationPastSpectacle() {
        // Given
        spectacle.setDate(LocalDateTime.now().minusDays(1));
        when(spectacleRepository.findByIdWithLock(1L)).thenReturn(Optional.of(spectacle));

        // When & Then
        assertThatThrownBy(() -> reservationService.createReservation(userId, reservationRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("passé");
        verify(spectacleRepository).findByIdWithLock(1L);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Doit récupérer les réservations d'un utilisateur avec pagination")
    void testGetUserReservations() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Reservation> reservationPage = new PageImpl<>(List.of(reservation));
        when(reservationRepository.findByKeycloakUserId(userId, pageable)).thenReturn(reservationPage);
        when(reservationMapper.toResponse(reservation)).thenReturn(reservationResponse);

        // When
        Page<ReservationResponse> result = reservationService.getUserReservations(userId, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(reservationRepository).findByKeycloakUserId(userId, pageable);
    }

    @Test
    @DisplayName("Doit récupérer une réservation par ID")
    void testGetReservationById() {
        // Given
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationMapper.toResponse(reservation)).thenReturn(reservationResponse);

        // When
        ReservationResponse result = reservationService.getReservationById(1L, userId);

        // Then
        assertThat(result).isNotNull();
        verify(reservationRepository).findById(1L);
        verify(reservationMapper).toResponse(reservation);
    }

    @Test
    @DisplayName("Doit lancer UnauthorizedAccessException si l'utilisateur n'est pas le propriétaire")
    void testGetReservationByIdUnauthorized() {
        // Given
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        // When & Then
        assertThatThrownBy(() -> reservationService.getReservationById(1L, "autre-user"))
                .isInstanceOf(UnauthorizedAccessException.class);
        verify(reservationRepository).findById(1L);
        verify(reservationMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Doit annuler une réservation avec succès")
    void testCancelReservation() {
        // Given
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        doNothing().when(reservationRepository).delete(reservation);
        when(spectacleRepository.save(spectacle)).thenReturn(spectacle);

        int initialTickets = spectacle.getAvailableTickets();

        // When
        reservationService.cancelReservation(1L, userId);

        // Then
        assertThat(spectacle.getAvailableTickets()).isEqualTo(initialTickets + 2); // Remise en disponibilité
        verify(reservationRepository).findById(1L);
        verify(reservationRepository).delete(reservation);
        verify(spectacleRepository).save(spectacle);
    }

    @Test
    @DisplayName("Doit lancer UnauthorizedAccessException lors de l'annulation par un autre utilisateur")
    void testCancelReservationUnauthorized() {
        // Given
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        // When & Then
        assertThatThrownBy(() -> reservationService.cancelReservation(1L, "autre-user"))
                .isInstanceOf(UnauthorizedAccessException.class);
        verify(reservationRepository).findById(1L);
        verify(reservationRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Doit lancer IllegalStateException si le spectacle est passé lors de l'annulation")
    void testCancelReservationPastSpectacle() {
        // Given
        spectacle.setDate(LocalDateTime.now().minusDays(1));
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        // When & Then
        assertThatThrownBy(() -> reservationService.cancelReservation(1L, userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("passé");
        verify(reservationRepository).findById(1L);
        verify(reservationRepository, never()).delete(any());
    }
}

