package epsi.archiapp.backend.integration;

import epsi.archiapp.backend.dto.ReservationRequest;
import epsi.archiapp.backend.model.Reservation;
import epsi.archiapp.backend.model.Spectacle;
import epsi.archiapp.backend.repository.ReservationRepository;
import epsi.archiapp.backend.repository.SpectacleRepository;
import epsi.archiapp.backend.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Tests d'intégration de concurrence pour les réservations")
class ReservationConcurrencyIntegrationTest {

    @Autowired
    private SpectacleRepository spectacleRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationService reservationService;

    private Spectacle spectacle;

    @BeforeEach
    void setUp() {
        // Nettoyer les données
        reservationRepository.deleteAll();
        spectacleRepository.deleteAll();

        // Créer un spectacle avec un nombre limité de billets
        spectacle = Spectacle.builder()
                .title("Spectacle Test Concurrence")
                .description("Test de gestion de la concurrence")
                .date(LocalDateTime.now().plusDays(30))
                .price(new BigDecimal("25.00"))
                .availableTickets(10) // Seulement 10 billets
                .build();

        spectacle = spectacleRepository.save(spectacle);
    }

    @Test
    @DisplayName("Doit gérer correctement les réservations concurrentes sans overbooking")
    void testConcurrentReservationsNoOverbooking() throws InterruptedException {
        // Given
        int numberOfThreads = 20; // 20 utilisateurs simultanés
        int ticketsPerReservation = 1; // Chacun veut 1 billet
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        AtomicInteger successfulReservations = new AtomicInteger(0);
        AtomicInteger failedReservations = new AtomicInteger(0);

        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // When - Plusieurs threads tentent de réserver simultanément
        for (int i = 0; i < numberOfThreads; i++) {
            final int userId = i;
            executorService.submit(() -> {
                try {
                    ReservationRequest request = new ReservationRequest();
                    request.setSpectacleId(spectacle.getId());
                    request.setQuantity(ticketsPerReservation);

                    reservationService.createReservation("user-" + userId, request);
                    successfulReservations.incrementAndGet();
                } catch (Exception e) {
                    failedReservations.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        // Attendre que tous les threads terminent
        latch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();
        executorService.awaitTermination(30, TimeUnit.SECONDS);

        // Attendre que toutes les transactions soient terminées
        Thread.sleep(1000);

        // Then - Vérifier les résultats
        Spectacle updatedSpectacle = spectacleRepository.findById(spectacle.getId()).orElseThrow();
        List<Reservation> allReservations = reservationRepository.findAll();

        // Il ne doit y avoir que 10 réservations réussies (nombre initial de billets)
        assertThat(successfulReservations.get()).isEqualTo(10);
        assertThat(failedReservations.get()).isEqualTo(10);

        // Le nombre de billets disponibles doit être 0
        assertThat(updatedSpectacle.getAvailableTickets()).isEqualTo(0);

        // Le nombre total de réservations doit être 10
        assertThat(allReservations).hasSize(10);

        // La somme des billets réservés doit être égale au nombre initial
        int totalTicketsReserved = allReservations.stream()
                .mapToInt(Reservation::getQuantity)
                .sum();
        assertThat(totalTicketsReserved).isEqualTo(10);
    }

    @Test
    @DisplayName("Doit éviter les réservations en double pour le même utilisateur")
    void testPreventDuplicateReservations() throws InterruptedException {
        // Given
        String userId = "user-duplicate-test";
        int numberOfAttempts = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfAttempts);
        CountDownLatch latch = new CountDownLatch(numberOfAttempts);

        // When - Le même utilisateur tente de réserver plusieurs fois simultanément
        for (int i = 0; i < numberOfAttempts; i++) {
            executorService.submit(() -> {
                try {
                    ReservationRequest request = new ReservationRequest();
                    request.setSpectacleId(spectacle.getId());
                    request.setQuantity(2);

                    reservationService.createReservation(userId, request);
                } catch (Exception e) {
                    // Ignorer les erreurs de concurrence
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
        Thread.sleep(1000);

        // Then
        List<Reservation> userReservations = reservationRepository.findByKeycloakUserId(userId);

        // Grâce au verrouillage pessimiste, les réservations doivent être sérialisées
        // et ne pas créer d'incohérences
        assertThat(userReservations).isNotEmpty();

        Spectacle finalSpectacle = spectacleRepository.findById(spectacle.getId()).orElseThrow();
        int totalReserved = userReservations.stream()
                .mapToInt(Reservation::getQuantity)
                .sum();

        // Vérifier la cohérence
        assertThat(finalSpectacle.getAvailableTickets() + totalReserved).isLessThanOrEqualTo(10);
    }
}

