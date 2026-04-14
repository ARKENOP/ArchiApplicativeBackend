package epsi.archiapp.backend.service;

import epsi.archiapp.backend.dto.SpectacleRequest;
import epsi.archiapp.backend.dto.SpectacleResponse;
import epsi.archiapp.backend.exception.ResourceNotFoundException;
import epsi.archiapp.backend.mapper.SpectacleMapper;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
    private SpectacleResponse response;

    @BeforeEach
    void setUp() {
        spectacle = Spectacle.builder()
                .id(1L)
                .title("Hamlet")
                .description("Classic")
                .date(LocalDateTime.now().plusDays(5))
                .price(new BigDecimal("25.00"))
                .availableTickets(100)
                .build();

        response = SpectacleResponse.builder()
                .id(1L)
                .title("Hamlet")
                .build();
    }

    @Test
    void listAll_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Spectacle> page = new PageImpl<>(List.of(spectacle), pageable, 1);

        when(spectacleRepository.findAll(pageable)).thenReturn(page);
        when(spectacleMapper.toResponse(spectacle)).thenReturn(response);

        Page<SpectacleResponse> result = spectacleService.listAll(pageable);

        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    void get_shouldThrowWhenSpectacleNotFound() {
        when(spectacleRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> spectacleService.get(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Spectacle");
    }

    @Test
    void create_shouldPersistAndReturnMappedResponse() {
        SpectacleRequest request = SpectacleRequest.builder().title("Nouveau").build();
        Spectacle toSave = Spectacle.builder().title("Nouveau").build();

        when(spectacleMapper.toEntity(request)).thenReturn(toSave);
        when(spectacleRepository.save(toSave)).thenReturn(spectacle);
        when(spectacleMapper.toResponse(spectacle)).thenReturn(response);

        SpectacleResponse result = spectacleService.create(request);

        assertThat(result).isEqualTo(response);
        verify(spectacleRepository).save(toSave);
    }

    @Test
    void update_shouldThrowWhenSpectacleNotFound() {
        SpectacleRequest request = SpectacleRequest.builder().title("Maj").build();
        when(spectacleRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> spectacleService.update(7L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(spectacleRepository, never()).save(any());
    }

    @Test
    void update_shouldApplyMapperAndSaveEntity() {
        SpectacleRequest request = SpectacleRequest.builder().title("Maj").build();
        when(spectacleRepository.findById(1L)).thenReturn(Optional.of(spectacle));
        when(spectacleRepository.save(spectacle)).thenReturn(spectacle);
        when(spectacleMapper.toResponse(spectacle)).thenReturn(response);

        SpectacleResponse result = spectacleService.update(1L, request);

        assertThat(result).isEqualTo(response);
        verify(spectacleMapper).updateEntityFromRequest(request, spectacle);
        verify(spectacleRepository).save(spectacle);
    }

    @Test
    void delete_shouldThrowWhenSpectacleDoesNotExist() {
        when(spectacleRepository.existsById(3L)).thenReturn(false);

        assertThatThrownBy(() -> spectacleService.delete(3L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Spectacle");

        verify(reservationRepository, never()).deleteBySpectacleId(anyLong());
        verify(spectacleRepository, never()).deleteById(anyLong());
    }

    @Test
    void delete_shouldDeleteReservationsThenSpectacle() {
        when(spectacleRepository.existsById(3L)).thenReturn(true);

        spectacleService.delete(3L);

        verify(reservationRepository).deleteBySpectacleId(3L);
        verify(spectacleRepository).deleteById(3L);
    }
}

