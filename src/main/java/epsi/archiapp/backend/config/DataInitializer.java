package epsi.archiapp.backend.config;

import epsi.archiapp.backend.model.Role;
import epsi.archiapp.backend.model.Spectacle;
import epsi.archiapp.backend.model.User;
import epsi.archiapp.backend.repository.RoleRepository;
import epsi.archiapp.backend.repository.SpectacleRepository;
import epsi.archiapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final SpectacleRepository spectacleRepository;

    @Bean
    CommandLineRunner init() {
        return args -> {
            Role admin = roleRepository.findByName("ADMIN").orElseGet(() -> roleRepository.save(Role.builder().name("ADMIN").build()));
            Role userRole = roleRepository.findByName("USER").orElseGet(() -> roleRepository.save(Role.builder().name("USER").build()));

            if (userRepository.count() == 0) {
                userRepository.save(User.builder()
                        .email("admin@example.com")
                        .username("admin")
                        .password("admin")
                        .role(admin)
                        .build());
                userRepository.save(User.builder()
                        .email("user@example.com")
                        .username("user")
                        .password("user")
                        .role(userRole)
                        .build());
            }

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
            }
        };
    }
}

