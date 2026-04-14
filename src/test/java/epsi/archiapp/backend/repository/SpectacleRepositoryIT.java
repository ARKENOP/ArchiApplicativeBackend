package epsi.archiapp.backend.repository;

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
class SpectacleRepositoryIT extends BasePostgresContainerIT {

    @Autowired
    private SpectacleRepository spectacleRepository;

    @BeforeEach
    void clean() {
        spectacleRepository.deleteAll();
    }

    @Test
    void findUpcomingSpectacles_shouldReturnOnlyFutureSpectacles() {
        Spectacle past = saveSpectacle("Past", "old", LocalDateTime.now().minusDays(1), 5);
        Spectacle futureA = saveSpectacle("Future A", "desc", LocalDateTime.now().plusDays(1), 5);
        Spectacle futureB = saveSpectacle("Future B", "desc", LocalDateTime.now().plusDays(2), 5);

        var result = spectacleRepository.findUpcomingSpectacles(LocalDateTime.now(), PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(Spectacle::getId)
                .contains(futureA.getId(), futureB.getId())
                .doesNotContain(past.getId());
    }

    @Test
    void findAvailableSpectacles_shouldFilterByTicketsAndDate() {
        Spectacle available = saveSpectacle("Hamlet", "tragedie", LocalDateTime.now().plusDays(2), 10);
        Spectacle soldOut = saveSpectacle("Sold out", "full", LocalDateTime.now().plusDays(2), 0);
        Spectacle past = saveSpectacle("Past", "old", LocalDateTime.now().minusDays(2), 20);

        List<Spectacle> result = spectacleRepository.findAvailableSpectacles(LocalDateTime.now());

        assertThat(result).extracting(Spectacle::getId)
                .contains(available.getId())
                .doesNotContain(soldOut.getId(), past.getId());
    }

    @Test
    void searchSpectacles_shouldSearchInTitleAndDescriptionCaseInsensitive() {
        Spectacle matchingByTitle = saveSpectacle("Tragedie du Cid", "classic", LocalDateTime.now().plusDays(4), 10);
        Spectacle matchingByDescription = saveSpectacle("Autre", "Une grande tragedie", LocalDateTime.now().plusDays(5), 10);
        Spectacle nonMatching = saveSpectacle("Jazz", "musique", LocalDateTime.now().plusDays(6), 10);

        var result = spectacleRepository.searchSpectacles("TRAGEDIE", PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(Spectacle::getId)
                .contains(matchingByTitle.getId(), matchingByDescription.getId())
                .doesNotContain(nonMatching.getId());
    }

    @Test
    void findByIdWithLock_shouldReturnExistingEntity() {
        Spectacle spectacle = saveSpectacle("Hamlet", "desc", LocalDateTime.now().plusDays(1), 10);

        var result = spectacleRepository.findByIdWithLock(spectacle.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Hamlet");
    }

    private Spectacle saveSpectacle(String title, String description, LocalDateTime date, int availableTickets) {
        Spectacle spectacle = Spectacle.builder()
                .title(title)
                .description(description)
                .date(date)
                .price(new BigDecimal("20.00"))
                .availableTickets(availableTickets)
                .imageUrl("https://example.test/img.png")
                .createdBy("test")
                .createdAt(LocalDateTime.now())
                .build();

        return spectacleRepository.save(spectacle);
    }
}


