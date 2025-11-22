package epsi.archiapp.backend.mapper;

import epsi.archiapp.backend.dto.SpectacleRequest;
import epsi.archiapp.backend.dto.SpectacleResponse;
import epsi.archiapp.backend.model.Spectacle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests du mapper Spectacle")
class SpectacleMapperTest {

    private SpectacleMapper spectacleMapper;
    private LocalDateTime futureDate;

    @BeforeEach
    void setUp() {
        spectacleMapper = new SpectacleMapper();
        futureDate = LocalDateTime.now().plusDays(30);
    }

    @Test
    @DisplayName("Doit convertir une entité Spectacle en SpectacleResponse")
    void testToResponse() {
        // Given
        Spectacle spectacle = Spectacle.builder()
                .id(1L)
                .title("Le Malade Imaginaire")
                .description("Comédie de Molière")
                .date(futureDate)
                .price(new BigDecimal("25.00"))
                .availableTickets(100)
                .imageUrl("https://example.com/image.jpg")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy("admin")
                .updatedBy("admin")
                .build();

        // When
        SpectacleResponse response = spectacleMapper.toResponse(spectacle);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Le Malade Imaginaire");
        assertThat(response.getDescription()).isEqualTo("Comédie de Molière");
        assertThat(response.getDate()).isEqualTo(futureDate);
        assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(response.getAvailableTickets()).isEqualTo(100);
        assertThat(response.getImageUrl()).isEqualTo("https://example.com/image.jpg");
        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getCreatedBy()).isEqualTo("admin");
    }

    @Test
    @DisplayName("Doit retourner null si l'entité est null")
    void testToResponseNull() {
        // When
        SpectacleResponse response = spectacleMapper.toResponse(null);

        // Then
        assertThat(response).isNull();
    }

    @Test
    @DisplayName("Doit convertir un SpectacleRequest en entité Spectacle")
    void testToEntity() {
        // Given
        SpectacleRequest request = SpectacleRequest.builder()
                .title("Hamlet")
                .description("Tragédie de Shakespeare")
                .date(futureDate)
                .price(new BigDecimal("30.00"))
                .availableTickets(150)
                .imageUrl("https://example.com/hamlet.jpg")
                .build();

        // When
        Spectacle spectacle = spectacleMapper.toEntity(request);

        // Then
        assertThat(spectacle).isNotNull();
        assertThat(spectacle.getId()).isNull(); // Pas encore persisté
        assertThat(spectacle.getTitle()).isEqualTo("Hamlet");
        assertThat(spectacle.getDescription()).isEqualTo("Tragédie de Shakespeare");
        assertThat(spectacle.getDate()).isEqualTo(futureDate);
        assertThat(spectacle.getPrice()).isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(spectacle.getAvailableTickets()).isEqualTo(150);
        assertThat(spectacle.getImageUrl()).isEqualTo("https://example.com/hamlet.jpg");
    }

    @Test
    @DisplayName("Doit retourner null si le request est null")
    void testToEntityNull() {
        // When
        Spectacle spectacle = spectacleMapper.toEntity(null);

        // Then
        assertThat(spectacle).isNull();
    }

    @Test
    @DisplayName("Doit mettre à jour une entité existante à partir d'un request")
    void testUpdateEntityFromRequest() {
        // Given
        Spectacle existingSpectacle = Spectacle.builder()
                .id(1L)
                .title("Ancien Titre")
                .description("Ancienne description")
                .date(LocalDateTime.now().plusDays(10))
                .price(new BigDecimal("20.00"))
                .availableTickets(50)
                .imageUrl("https://example.com/old.jpg")
                .build();

        SpectacleRequest updateRequest = SpectacleRequest.builder()
                .title("Nouveau Titre")
                .description("Nouvelle description")
                .date(futureDate)
                .price(new BigDecimal("35.00"))
                .availableTickets(200)
                .imageUrl("https://example.com/new.jpg")
                .build();

        // When
        spectacleMapper.updateEntityFromRequest(updateRequest, existingSpectacle);

        // Then
        assertThat(existingSpectacle.getId()).isEqualTo(1L); // L'ID ne change pas
        assertThat(existingSpectacle.getTitle()).isEqualTo("Nouveau Titre");
        assertThat(existingSpectacle.getDescription()).isEqualTo("Nouvelle description");
        assertThat(existingSpectacle.getDate()).isEqualTo(futureDate);
        assertThat(existingSpectacle.getPrice()).isEqualByComparingTo(new BigDecimal("35.00"));
        assertThat(existingSpectacle.getAvailableTickets()).isEqualTo(200);
        assertThat(existingSpectacle.getImageUrl()).isEqualTo("https://example.com/new.jpg");
    }

    @Test
    @DisplayName("Ne doit rien faire si le request ou l'entité est null")
    void testUpdateEntityFromRequestNull() {
        // Given
        Spectacle spectacle = Spectacle.builder()
                .title("Original")
                .build();

        // When
        spectacleMapper.updateEntityFromRequest(null, spectacle);

        // Then
        assertThat(spectacle.getTitle()).isEqualTo("Original"); // Pas de changement

        // When
        spectacleMapper.updateEntityFromRequest(SpectacleRequest.builder().build(), null);

        // Then - Pas d'exception levée
    }
}

