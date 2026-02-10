package epsi.archiapp.backend.config;

import epsi.archiapp.backend.model.Reservation;
import epsi.archiapp.backend.model.Spectacle;
import epsi.archiapp.backend.repository.ReservationRepository;
import epsi.archiapp.backend.repository.SpectacleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final SpectacleRepository spectacleRepository;
    private final ReservationRepository reservationRepository;

    private static final String[] KEYCLOAK_USER_IDS = {
            "paul",
            "manyonne",
            "testuser23",
            "testuser24",
            "testuser25"
    };

    @Bean
    CommandLineRunner init() {
        return args -> {
            if (spectacleRepository.count() == 0) {
                log.info("Initializing database with fixture data...");

                List<Spectacle> spectacles = createSpectacles();
                spectacleRepository.saveAll(spectacles);
                log.info("Created {} spectacles", spectacles.size());

                List<Reservation> reservations = createReservations(spectacles);
                reservationRepository.saveAll(reservations);
                log.info("Created {} reservations", reservations.size());

                log.info("Database initialization completed successfully");
            } else {
                log.info("Database already contains data, skipping initialization");
            }
        };
    }

    private List<Spectacle> createSpectacles() {
        List<Spectacle> spectacles = new ArrayList<>();

        // Théâtre classique
        spectacles.add(Spectacle.builder()
                .title("Hamlet")
                .description("La tragédie de Shakespeare mise en scène par Thomas Ostermeier. Un prince du Danemark tourmenté par la vengeance et la folie.")
                .date(LocalDateTime.now().plusDays(10).withHour(20).withMinute(30))
                .price(new BigDecimal("25.00"))
                .availableTickets(100)
                .imageUrl("https://picsum.photos/seed/hamlet/600/400")
                .build());

        spectacles.add(Spectacle.builder()
                .title("Le Cid")
                .description("La tragicomédie de Corneille. L'histoire de Rodrigue et Chimène, déchirés entre l'amour et l'honneur.")
                .date(LocalDateTime.now().plusDays(20).withHour(19).withMinute(0))
                .price(new BigDecimal("19.90"))
                .availableTickets(80)
                .imageUrl("https://picsum.photos/seed/lecid/600/400")
                .build());

        spectacles.add(Spectacle.builder()
                .title("Roméo et Juliette")
                .description("La plus célèbre histoire d'amour de Shakespeare. Une version moderne et épurée.")
                .date(LocalDateTime.now().plusDays(15).withHour(20).withMinute(0))
                .price(new BigDecimal("28.00"))
                .availableTickets(120)
                .imageUrl("https://picsum.photos/seed/romeo/600/400")
                .build());

        spectacles.add(Spectacle.builder()
                .title("Cyrano de Bergerac")
                .description("L'œuvre d'Edmond Rostand. Le poète au grand nez et au grand cœur dans une mise en scène flamboyante.")
                .date(LocalDateTime.now().plusDays(25).withHour(20).withMinute(30))
                .price(new BigDecimal("32.00"))
                .availableTickets(150)
                .imageUrl("https://picsum.photos/seed/cyrano/600/400")
                .build());

        // Ballet
        spectacles.add(Spectacle.builder()
                .title("Le Lac des Cygnes")
                .description("Ballet classique de Tchaïkovski. L'histoire du prince Siegfried et de la princesse Odette transformée en cygne.")
                .date(LocalDateTime.now().plusDays(30).withHour(19).withMinute(30))
                .price(new BigDecimal("45.00"))
                .availableTickets(150)
                .imageUrl("https://picsum.photos/seed/swanlake/600/400")
                .build());

        spectacles.add(Spectacle.builder()
                .title("Casse-Noisette")
                .description("Le ballet féerique de Tchaïkovski. Clara et son voyage au pays des bonbons dans une chorégraphie éblouissante.")
                .date(LocalDateTime.now().plusDays(35).withHour(15).withMinute(0))
                .price(new BigDecimal("42.00"))
                .availableTickets(200)
                .imageUrl("https://picsum.photos/seed/nutcracker/600/400")
                .build());

        spectacles.add(Spectacle.builder()
                .title("Giselle")
                .description("Ballet romantique en deux actes. L'histoire d'une jeune paysanne qui meurt de chagrin d'amour.")
                .date(LocalDateTime.now().plusDays(45).withHour(20).withMinute(0))
                .price(new BigDecimal("40.00"))
                .availableTickets(130)
                .imageUrl("https://picsum.photos/seed/giselle/600/400")
                .build());

        // Opéra
        spectacles.add(Spectacle.builder()
                .title("Carmen")
                .description("Opéra de Bizet. L'histoire passionnée de Carmen, la gitane indomptable, et de Don José.")
                .date(LocalDateTime.now().plusDays(40).withHour(19).withMinute(30))
                .price(new BigDecimal("38.50"))
                .availableTickets(120)
                .imageUrl("https://picsum.photos/seed/carmen/600/400")
                .build());

        spectacles.add(Spectacle.builder()
                .title("La Traviata")
                .description("L'opéra de Verdi. Les amours tragiques de Violetta Valéry et d'Alfredo Germont.")
                .date(LocalDateTime.now().plusDays(50).withHour(20).withMinute(0))
                .price(new BigDecimal("55.00"))
                .availableTickets(140)
                .imageUrl("https://picsum.photos/seed/traviata/600/400")
                .build());

        spectacles.add(Spectacle.builder()
                .title("La Flûte Enchantée")
                .description("Opéra de Mozart. Le conte initiatique de Tamino à la recherche de Pamina.")
                .date(LocalDateTime.now().plusDays(55).withHour(19).withMinute(0))
                .price(new BigDecimal("48.00"))
                .availableTickets(160)
                .imageUrl("https://picsum.photos/seed/fluteenchantee/600/400")
                .build());

        // Théâtre contemporain
        spectacles.add(Spectacle.builder()
                .title("Le Prénom")
                .description("Comédie théâtrale de Matthieu Delaporte et Alexandre de La Patellière. Un dîner entre amis qui tourne au vinaigre.")
                .date(LocalDateTime.now().plusDays(12).withHour(21).withMinute(0))
                .price(new BigDecimal("32.00"))
                .availableTickets(90)
                .imageUrl("https://picsum.photos/seed/leprenom/600/400")
                .build());

        spectacles.add(Spectacle.builder()
                .title("Le Cercle des Illusionnistes")
                .description("Un spectacle mêlant théâtre et magie. Plongez dans l'univers mystérieux des illusionnistes du début du XXe siècle.")
                .date(LocalDateTime.now().plusDays(18).withHour(20).withMinute(30))
                .price(new BigDecimal("36.00"))
                .availableTickets(110)
                .imageUrl("https://picsum.photos/seed/illusionnistes/600/400")
                .build());

        spectacles.add(Spectacle.builder()
                .title("Art")
                .description("La pièce de Yasmina Reza. Trois amis, un tableau blanc et beaucoup de questions sur l'art contemporain.")
                .date(LocalDateTime.now().plusDays(22).withHour(19).withMinute(30))
                .price(new BigDecimal("29.00"))
                .availableTickets(85)
                .imageUrl("https://picsum.photos/seed/art/600/400")
                .build());

        // Concerts
        spectacles.add(Spectacle.builder()
                .title("Orchestre Symphonique - Beethoven")
                .description("Concert exceptionnel avec la Symphonie n°9 de Beethoven et l'Hymne à la joie.")
                .date(LocalDateTime.now().plusDays(28).withHour(20).withMinute(0))
                .price(new BigDecimal("52.00"))
                .availableTickets(180)
                .imageUrl("https://picsum.photos/seed/beethoven/600/400")
                .build());

        spectacles.add(Spectacle.builder()
                .title("Jazz Night - Tribute to Miles Davis")
                .description("Soirée jazz exceptionnelle en hommage à Miles Davis. Quintet de renommée internationale.")
                .date(LocalDateTime.now().plusDays(33).withHour(21).withMinute(0))
                .price(new BigDecimal("38.00"))
                .availableTickets(95)
                .imageUrl("https://picsum.photos/seed/jazz/600/400")
                .build());

        // One-man show
        spectacles.add(Spectacle.builder()
                .title("Gad Elmaleh - D'ailleurs")
                .description("Le one-man show événement de Gad Elmaleh. Entre la France et l'Amérique, ses nouvelles aventures.")
                .date(LocalDateTime.now().plusDays(8).withHour(20).withMinute(30))
                .price(new BigDecimal("45.00"))
                .availableTickets(250)
                .imageUrl("https://picsum.photos/seed/gadelmaleh/600/400")
                .build());

        spectacles.add(Spectacle.builder()
                .title("Florence Foresti - Epilogue")
                .description("Le spectacle de Florence Foresti qui a fait salle comble à travers toute la France.")
                .date(LocalDateTime.now().plusDays(14).withHour(21).withMinute(0))
                .price(new BigDecimal("42.00"))
                .availableTickets(220)
                .imageUrl("https://picsum.photos/seed/foresti/600/400")
                .build());

        // Spectacles jeunesse
        spectacles.add(Spectacle.builder()
                .title("Le Petit Prince")
                .description("L'adaptation théâtrale du chef-d'œuvre de Saint-Exupéry. Pour petits et grands.")
                .date(LocalDateTime.now().plusDays(16).withHour(15).withMinute(0))
                .price(new BigDecimal("18.00"))
                .availableTickets(140)
                .imageUrl("https://picsum.photos/seed/petitprince/600/400")
                .build());

        spectacles.add(Spectacle.builder()
                .title("Le Roi Lion - Comédie Musicale")
                .description("La comédie musicale Disney adaptée du film d'animation. Spectacle familial époustouflant.")
                .date(LocalDateTime.now().plusDays(60).withHour(14).withMinute(30))
                .price(new BigDecimal("65.00"))
                .availableTickets(300)
                .imageUrl("https://picsum.photos/seed/roilion/600/400")
                .build());

        // Spectacles presque complets
        spectacles.add(Spectacle.builder()
                .title("Notre-Dame de Paris - Le Musical")
                .description("La comédie musicale culte de Luc Plamondon et Richard Cocciante. Dernières places disponibles!")
                .date(LocalDateTime.now().plusDays(7).withHour(20).withMinute(0))
                .price(new BigDecimal("58.00"))
                .availableTickets(15)
                .imageUrl("https://picsum.photos/seed/notredame/600/400")
                .build());

        return spectacles;
    }

    private List<Reservation> createReservations(List<Spectacle> spectacles) {
        List<Reservation> reservations = new ArrayList<>();


        // Hamlet - Quelques réservations
        if (spectacles.size() > 0) {
            Spectacle hamlet = spectacles.get(0);
            reservations.add(Reservation.builder()
                    .spectacle(hamlet)
                    .keycloakUserId(KEYCLOAK_USER_IDS[0])
                    .quantity(2)
                    .totalPrice(hamlet.getPrice().multiply(new BigDecimal("2")))
                    .build());
            reservations.add(Reservation.builder()
                    .spectacle(hamlet)
                    .keycloakUserId(KEYCLOAK_USER_IDS[1])
                    .quantity(4)
                    .totalPrice(hamlet.getPrice().multiply(new BigDecimal("4")))
                    .build());
        }

        // Le Lac des Cygnes - Populaire
        if (spectacles.size() > 4) {
            Spectacle lacDesCygnes = spectacles.get(4);
            reservations.add(Reservation.builder()
                    .spectacle(lacDesCygnes)
                    .keycloakUserId(KEYCLOAK_USER_IDS[2])
                    .quantity(2)
                    .totalPrice(lacDesCygnes.getPrice().multiply(new BigDecimal("2")))
                    .build());
            reservations.add(Reservation.builder()
                    .spectacle(lacDesCygnes)
                    .keycloakUserId(KEYCLOAK_USER_IDS[3])
                    .quantity(3)
                    .totalPrice(lacDesCygnes.getPrice().multiply(new BigDecimal("3")))
                    .build());
            reservations.add(Reservation.builder()
                    .spectacle(lacDesCygnes)
                    .keycloakUserId(KEYCLOAK_USER_IDS[4])
                    .quantity(1)
                    .totalPrice(lacDesCygnes.getPrice())
                    .build());
        }

        // Carmen - Quelques places réservées
        if (spectacles.size() > 7) {
            Spectacle carmen = spectacles.get(7);
            reservations.add(Reservation.builder()
                    .spectacle(carmen)
                    .keycloakUserId(KEYCLOAK_USER_IDS[0])
                    .quantity(2)
                    .totalPrice(carmen.getPrice().multiply(new BigDecimal("2")))
                    .build());
        }

        // Gad Elmaleh - Très populaire
        if (spectacles.size() > 15) {
            Spectacle gadElmaleh = spectacles.get(15);
            reservations.add(Reservation.builder()
                    .spectacle(gadElmaleh)
                    .keycloakUserId(KEYCLOAK_USER_IDS[1])
                    .quantity(3)
                    .totalPrice(gadElmaleh.getPrice().multiply(new BigDecimal("3")))
                    .build());
            reservations.add(Reservation.builder()
                    .spectacle(gadElmaleh)
                    .keycloakUserId(KEYCLOAK_USER_IDS[2])
                    .quantity(2)
                    .totalPrice(gadElmaleh.getPrice().multiply(new BigDecimal("2")))
                    .build());
            reservations.add(Reservation.builder()
                    .spectacle(gadElmaleh)
                    .keycloakUserId(KEYCLOAK_USER_IDS[3])
                    .quantity(4)
                    .totalPrice(gadElmaleh.getPrice().multiply(new BigDecimal("4")))
                    .build());
        }

        // Notre-Dame de Paris - Presque complet (beaucoup de réservations)
        if (spectacles.size() > 19) {
            Spectacle notreDame = spectacles.get(19);
            for (int i = 0; i < KEYCLOAK_USER_IDS.length; i++) {
                reservations.add(Reservation.builder()
                        .spectacle(notreDame)
                        .keycloakUserId(KEYCLOAK_USER_IDS[i])
                        .quantity(i % 3 + 1)
                        .totalPrice(notreDame.getPrice().multiply(new BigDecimal(String.valueOf(i % 3 + 1))))
                        .build());
            }
        }

        return reservations;
    }
}
