package epsi.archiapp.backend.config;

import epsi.archiapp.backend.model.Spectacle;
import epsi.archiapp.backend.repository.SpectacleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final SpectacleRepository spectacleRepository;

    @Bean
    CommandLineRunner init() {
        return args -> {
            if (spectacleRepository.count() == 0) {
                spectacleRepository.save(Spectacle.builder()
                        .title("Hamlet")
                        .description("La tragédie de Shakespeare")
                        .date(LocalDateTime.now().plusDays(10))
                        .price(new BigDecimal("25.00"))
                        .availableTickets(100)
                        .imageUrl("https://picsum.photos/seed/hamlet/600/400")
                        .build());
                spectacleRepository.save(Spectacle.builder()
                        .title("Le Cid")
                        .description("Corneille sur scène")
                        .date(LocalDateTime.now().plusDays(20))
                        .price(new BigDecimal("19.90"))
                        .availableTickets(80)
                        .imageUrl("https://picsum.photos/seed/lecid/600/400")
                        .build());
                spectacleRepository.save(Spectacle.builder()
                        .title("Le Lac des Cygnes")
                        .description("Ballet classique de Tchaïkovski")
                        .date(LocalDateTime.now().plusDays(30))
                        .price(new BigDecimal("45.00"))
                        .availableTickets(150)
                        .imageUrl("https://picsum.photos/seed/swanlake/600/400")
                        .build());
                spectacleRepository.save(Spectacle.builder()
                        .title("Carmen")
                        .description("Opéra de Bizet")
                        .date(LocalDateTime.now().plusDays(40))
                        .price(new BigDecimal("38.50"))
                        .availableTickets(120)
                        .imageUrl("https://picsum.photos/seed/carmen/600/400")
                        .build());
            }
        };
    }
}

