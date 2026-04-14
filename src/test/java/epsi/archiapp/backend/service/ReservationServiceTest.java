package epsi.archiapp.backend.service;

import epsi.archiapp.backend.dto.ReservationRequest;
import epsi.archiapp.backend.dto.ReservationResponse;
import epsi.archiapp.backend.dto.StatsResponse;
import epsi.archiapp.backend.exception.InsufficientTicketsException;
import epsi.archiapp.backend.exception.ResourceNotFoundException;
import epsi.archiapp.backend.exception.UnauthorizedAccessException;
import epsi.archiapp.backend.mapper.ReservationMapper;
import epsi.archiapp.backend.model.Reservation;
import epsi.archiapp.backend.model.Spectacle;
import epsi.archiapp.backend.repository.ReservationRepository;
import epsi.archiapp.backend.repository.SpectacleRepository;
import org.junit.jupiter.api.BeforeEach;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
    private ReservationRequest request;

    @BeforeEach
    void setUp() {
        spectacle = Spectacle.builder()
                .id(10L)
                .title("Hamlet")
                .price(new BigDecimal("30.00"))
                .availableTickets(50)
                .date(LocalDateTime.now().plusDays(2))
                .build();

        request = new ReservationRequest(10L, 2);
    }

    @Test
    void createReservation_shouldCreateReservationWhenInputIsValid() {
        Reservation savedReservation = Reservation.builder().id(99L).spectacle(spectacle).quantity(2).build();
        ReservationResponse expectedResponse = ReservationResponse.builder().id(99L).build();

        when(spectacleRepository.findByIdWithLock(10L)).thenReturn(Optional.of(spectacle));
        when(reservationMapper.toEntity(eq(request), eq(spectacle), eq("user-1"), eq(new BigDecimal("60.00"))))
                .thenReturn(savedReservation);
        when(reservationRepository.save(savedReservation)).thenReturn(savedReservation);
        when(reservationMapper.toResponse(savedReservation)).thenReturn(expectedResponse);

        ReservationResponse result = reservationService.createReservation("user-1", request);

        assertThat(result).isEqualTo(expectedResponse);
        assertThat(spectacle.getAvailableTickets()).isEqualTo(48);
        verify(spectacleRepository).save(spectacle);
        verify(reservationRepository).save(savedReservation);
    }

    @Test
    void createReservation_shouldThrowWhenSpectacleDoesNotExist() {
        when(spectacleRepository.findByIdWithLock(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.createReservation("user-1", request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Spectacle");

        verifyNoInteractions(reservationMapper);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_shouldThrowWhenTicketsAreInsufficient() {
        spectacle.setAvailableTickets(1);
        when(spectacleRepository.findByIdWithLock(10L)).thenReturn(Optional.of(spectacle));

        assertThatThrownBy(() -> reservationService.createReservation("user-1", request))
                .isInstanceOf(InsufficientTicketsException.class)
                .hasMessageContaining("Pas assez de billets");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_shouldThrowWhenSpectacleIsInPast() {
        spectacle.setDate(LocalDateTime.now().minusDays(1));
        when(spectacleRepository.findByIdWithLock(10L)).thenReturn(Optional.of(spectacle));

        assertThatThrownBy(() -> reservationService.createReservation("user-1", request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spectacle passé");
    }

    @Test
    void getUserReservations_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Reservation reservation = Reservation.builder().id(1L).spectacle(spectacle).build();
        ReservationResponse response = ReservationResponse.builder().id(1L).build();
        Page<Reservation> page = new PageImpl<>(List.of(reservation), pageable, 1);

        when(reservationRepository.findByKeycloakUserId("user-1", pageable)).thenReturn(page);
        when(reservationMapper.toResponse(reservation)).thenReturn(response);

        Page<ReservationResponse> result = reservationService.getUserReservations("user-1", pageable);

        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    void getReservationById_shouldThrowWhenReservationBelongsToAnotherUser() {
        Reservation reservation = Reservation.builder().id(5L).keycloakUserId("owner").spectacle(spectacle).build();
        when(reservationRepository.findById(5L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.getReservationById(5L, "other-user"))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("autorisé");
    }

    @Test
    void cancelReservation_shouldRestoreTicketsAndDeleteReservation() {
        Reservation reservation = Reservation.builder()
                .id(7L)
                .quantity(3)
                .keycloakUserId("user-1")
                .spectacle(spectacle)
                .build();

        when(reservationRepository.findById(7L)).thenReturn(Optional.of(reservation));

        reservationService.cancelReservation(7L, "user-1");

        assertThat(spectacle.getAvailableTickets()).isEqualTo(53);
        verify(spectacleRepository).save(spectacle);
        verify(reservationRepository).delete(reservation);
    }

    @Test
    void cancelReservation_shouldThrowWhenSpectacleIsInPast() {
        spectacle.setDate(LocalDateTime.now().minusHours(2));
        Reservation reservation = Reservation.builder()
                .id(7L)
                .quantity(3)
                .keycloakUserId("user-1")
                .spectacle(spectacle)
                .build();
        when(reservationRepository.findById(7L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.cancelReservation(7L, "user-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spectacle passé");
    }

    @Test
    void getStatistics_shouldReturnZeroRevenueWhenNoSales() {
        when(reservationRepository.getTotalSales()).thenReturn(null);
        when(reservationRepository.count()).thenReturn(0L);
        when(reservationRepository.getSalesBySpectacle()).thenReturn(List.of());

        StatsResponse result = reservationService.getStatistics();

        assertThat(result.getTotalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getTotalReservations()).isZero();
        assertThat(result.getSalesBySpectacle()).isEmpty();
    }

    @Test
    void getStatistics_shouldMapSalesBySpectacle() {
        ReservationRepository.SalesBySpectacle row = new ReservationRepository.SalesBySpectacle() {
            @Override
            public Long getSpectacleId() {
                return 1L;
            }

            @Override
            public String getTitle() {
                return "Hamlet";
            }

            @Override
            public Long getTicketsSold() {
                return 9L;
            }

            @Override
            public BigDecimal getRevenue() {
                return new BigDecimal("270.00");
            }
        };

        when(reservationRepository.getTotalSales()).thenReturn(new BigDecimal("270.00"));
        when(reservationRepository.count()).thenReturn(3L);
        when(reservationRepository.getSalesBySpectacle()).thenReturn(List.of(row));

        StatsResponse result = reservationService.getStatistics();

        assertThat(result.getTotalRevenue()).isEqualByComparingTo("270.00");
        assertThat(result.getSalesBySpectacle()).hasSize(1);
        assertThat(result.getSalesBySpectacle().getFirst().getTitle()).isEqualTo("Hamlet");
    }
}

