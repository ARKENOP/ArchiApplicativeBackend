package epsi.archiapp.backend.repository;

import epsi.archiapp.backend.model.Spectacle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Tests du repository Spectacle")
class SpectacleRepositoryTest {

    @Autowired
    private SpectacleRepository spectacleRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Spectacle spectacle1;
    private Spectacle spectacle2;

    @BeforeEach
    void setUp() {
        spectacle1 = Spectacle.builder()
                .title("Le Malade Imaginaire")
                .description("Comédie de Molière")
                .date(LocalDateTime.now().plusDays(30))
                .price(new BigDecimal("25.00"))
                .availableTickets(100)
                .imageUrl("https://example.com/image1.jpg")
                .build();

        spectacle2 = Spectacle.builder()
                .title("Hamlet")
                .description("Tragédie de Shakespeare")
                .date(LocalDateTime.now().plusDays(60))
                .price(new BigDecimal("30.00"))
                .availableTickets(150)
                .imageUrl("https://example.com/image2.jpg")
                .build();

        entityManager.persist(spectacle1);
        entityManager.persist(spectacle2);
        entityManager.flush();
    }

    @Test
    @DisplayName("Doit sauvegarder et récupérer un spectacle")
    void testSaveAndFind() {
        // Given
        Spectacle newSpectacle = Spectacle.builder()
                .title("Tartuffe")
                .description("Comédie de Molière")
                .date(LocalDateTime.now().plusDays(45))
                .price(new BigDecimal("28.00"))
                .availableTickets(120)
                .build();

        // When
        Spectacle saved = spectacleRepository.save(newSpectacle);
        Optional<Spectacle> found = spectacleRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Tartuffe");
        assertThat(found.get().getPrice()).isEqualByComparingTo(new BigDecimal("28.00"));
    }

    @Test
    @DisplayName("Doit trouver un spectacle par titre")
    void testFindByTitle() {
        // When
        Spectacle found = spectacleRepository.findByTitle("Hamlet");

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getTitle()).isEqualTo("Hamlet");
        assertThat(found.getDescription()).contains("Shakespeare");
    }

    @Test
    @DisplayName("Doit récupérer les spectacles à venir")
    void testFindUpcomingSpectacles() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Spectacle> upcomingSpectacles = spectacleRepository.findUpcomingSpectacles(
                LocalDateTime.now(), pageable);

        // Then
        assertThat(upcomingSpectacles.getContent()).isNotEmpty();
        assertThat(upcomingSpectacles.getContent()).hasSize(2);
        assertThat(upcomingSpectacles.getContent().get(0).getDate()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("Doit récupérer les spectacles disponibles")
    void testFindAvailableSpectacles() {
        // When
        List<Spectacle> availableSpectacles = spectacleRepository.findAvailableSpectacles(LocalDateTime.now());

        // Then
        assertThat(availableSpectacles).isNotEmpty();
        assertThat(availableSpectacles).hasSize(2);
        assertThat(availableSpectacles).allMatch(s -> s.getAvailableTickets() > 0);
    }

    @Test
    @DisplayName("Doit rechercher des spectacles par mot-clé")
    void testSearchSpectacles() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Spectacle> results = spectacleRepository.searchSpectacles("molière", pageable);

        // Then
        assertThat(results.getContent()).isNotEmpty();
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getTitle()).isEqualTo("Le Malade Imaginaire");
    }

    @Test
    @DisplayName("Doit trouver un spectacle avec verrouillage")
    void testFindByIdWithLock() {
        // When - Utiliser le verrouillage optimiste pour H2
        Optional<Spectacle> found = spectacleRepository.findByIdWithOptimisticLock(spectacle1.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Le Malade Imaginaire");
    }

    @Test
    @DisplayName("Doit mettre à jour le nombre de billets disponibles")
    void testUpdateAvailableTickets() {
        // Given
        Spectacle spectacle = spectacleRepository.findById(spectacle1.getId()).orElseThrow();
        int initialTickets = spectacle.getAvailableTickets();

        // When
        spectacle.setAvailableTickets(initialTickets - 10);
        spectacleRepository.save(spectacle);
        entityManager.flush();
        entityManager.clear();

        // Then
        Spectacle updated = spectacleRepository.findById(spectacle1.getId()).orElseThrow();
        assertThat(updated.getAvailableTickets()).isEqualTo(initialTickets - 10);
    }

    @Test
    @DisplayName("Doit supprimer un spectacle")
    void testDeleteSpectacle() {
        // Given
        Long spectacleId = spectacle1.getId();

        // When
        spectacleRepository.deleteById(spectacleId);
        entityManager.flush();

        // Then
        Optional<Spectacle> deleted = spectacleRepository.findById(spectacleId);
        assertThat(deleted).isEmpty();
    }
}

