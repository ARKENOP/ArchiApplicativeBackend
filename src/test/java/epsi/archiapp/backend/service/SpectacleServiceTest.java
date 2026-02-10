package epsi.archiapp.backend.service;

import epsi.archiapp.backend.dto.SpectacleRequest;
import epsi.archiapp.backend.dto.SpectacleResponse;
import epsi.archiapp.backend.exception.ResourceNotFoundException;
import epsi.archiapp.backend.mapper.SpectacleMapper;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du service Spectacle")
class SpectacleServiceTest {

    @Mock
    private SpectacleRepository spectacleRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private SpectacleMapper spectacleMapper;

    @InjectMocks
    private SpectacleService spectacleService;

    private Spectacle spectacle;
    private SpectacleRequest spectacleRequest;
    private SpectacleResponse spectacleResponse;

    @BeforeEach
    void setUp() {
        spectacle = Spectacle.builder()
                .id(1L)
                .title("Le Malade Imaginaire")
                .description("Comédie de Molière")
                .date(LocalDateTime.now().plusDays(30))
                .price(new BigDecimal("25.00"))
                .availableTickets(100)
                .imageUrl("https://example.com/image.jpg")
                .build();

        spectacleRequest = SpectacleRequest.builder()
                .title("Le Malade Imaginaire")
                .description("Comédie de Molière")
                .date(LocalDateTime.now().plusDays(30))
                .price(new BigDecimal("25.00"))
                .availableTickets(100)
                .imageUrl("https://example.com/image.jpg")
                .build();

        spectacleResponse = SpectacleResponse.builder()
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
    @DisplayName("Doit lister tous les spectacles avec pagination")
    void testListAll() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Spectacle> spectaclePage = new PageImpl<>(List.of(spectacle));
        when(spectacleRepository.findAll(pageable)).thenReturn(spectaclePage);
        when(spectacleMapper.toResponse(spectacle)).thenReturn(spectacleResponse);

        // When
        Page<SpectacleResponse> result = spectacleService.listAll(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Le Malade Imaginaire");
        verify(spectacleRepository).findAll(pageable);
        verify(spectacleMapper).toResponse(spectacle);
    }

    @Test
    @DisplayName("Doit récupérer un spectacle par son ID")
    void testGet() {
        // Given
        when(spectacleRepository.findById(1L)).thenReturn(Optional.of(spectacle));
        when(spectacleMapper.toResponse(spectacle)).thenReturn(spectacleResponse);

        // When
        SpectacleResponse result = spectacleService.get(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Le Malade Imaginaire");
        verify(spectacleRepository).findById(1L);
        verify(spectacleMapper).toResponse(spectacle);
    }

    @Test
    @DisplayName("Doit lancer ResourceNotFoundException si le spectacle n'existe pas")
    void testGetNotFound() {
        // Given
        when(spectacleRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> spectacleService.get(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Spectacle");
        verify(spectacleRepository).findById(999L);
        verify(spectacleMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Doit créer un nouveau spectacle")
    void testCreate() {
        // Given
        when(spectacleMapper.toEntity(spectacleRequest)).thenReturn(spectacle);
        when(spectacleRepository.save(spectacle)).thenReturn(spectacle);
        when(spectacleMapper.toResponse(spectacle)).thenReturn(spectacleResponse);

        // When
        SpectacleResponse result = spectacleService.create(spectacleRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Le Malade Imaginaire");
        verify(spectacleMapper).toEntity(spectacleRequest);
        verify(spectacleRepository).save(spectacle);
        verify(spectacleMapper).toResponse(spectacle);
    }

    @Test
    @DisplayName("Doit mettre à jour un spectacle existant")
    void testUpdate() {
        // Given
        when(spectacleRepository.findById(1L)).thenReturn(Optional.of(spectacle));
        doNothing().when(spectacleMapper).updateEntityFromRequest(spectacleRequest, spectacle);
        when(spectacleRepository.save(spectacle)).thenReturn(spectacle);
        when(spectacleMapper.toResponse(spectacle)).thenReturn(spectacleResponse);

        // When
        SpectacleResponse result = spectacleService.update(1L, spectacleRequest);

        // Then
        assertThat(result).isNotNull();
        verify(spectacleRepository).findById(1L);
        verify(spectacleMapper).updateEntityFromRequest(spectacleRequest, spectacle);
        verify(spectacleRepository).save(spectacle);
        verify(spectacleMapper).toResponse(spectacle);
    }

    @Test
    @DisplayName("Doit lancer ResourceNotFoundException lors de la mise à jour d'un spectacle inexistant")
    void testUpdateNotFound() {
        // Given
        when(spectacleRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> spectacleService.update(999L, spectacleRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Spectacle");
        verify(spectacleRepository).findById(999L);
        verify(spectacleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Doit supprimer un spectacle existant et ses réservations associées")
    void testDelete() {
        // Given
        when(spectacleRepository.existsById(1L)).thenReturn(true);
        doNothing().when(reservationRepository).deleteBySpectacleId(1L);
        doNothing().when(spectacleRepository).deleteById(1L);

        // When
        spectacleService.delete(1L);

        // Then
        verify(spectacleRepository).existsById(1L);
        verify(reservationRepository).deleteBySpectacleId(1L);
        verify(spectacleRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Doit lancer ResourceNotFoundException lors de la suppression d'un spectacle inexistant")
    void testDeleteNotFound() {
        // Given
        when(spectacleRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> spectacleService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Spectacle");
        verify(spectacleRepository).existsById(999L);
        verify(spectacleRepository, never()).deleteById(any());
    }
}

