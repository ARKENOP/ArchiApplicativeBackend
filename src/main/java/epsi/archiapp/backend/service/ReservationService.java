package epsi.archiapp.backend.service;

import epsi.archiapp.backend.dto.ReservationRequest;
import epsi.archiapp.backend.dto.ReservationResponse;
import epsi.archiapp.backend.dto.StatsResponse;
import epsi.archiapp.backend.model.Reservation;
import epsi.archiapp.backend.model.Spectacle;
import epsi.archiapp.backend.repository.ReservationRepository;
import epsi.archiapp.backend.repository.SpectacleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final SpectacleRepository spectacleRepository;

    @Transactional
    public ReservationResponse createReservation(String keycloakUserId, ReservationRequest request) {
        // Récupérer le spectacle
        Spectacle spectacle = spectacleRepository.findById(request.getSpectacleId())
                .orElseThrow(() -> new IllegalArgumentException("Spectacle non trouvé avec l'ID: " + request.getSpectacleId()));

        // Vérifier la disponibilité
        if (spectacle.getAvailableTickets() < request.getQuantity()) {
            throw new IllegalStateException("Pas assez de billets disponibles. Disponibles: " +
                    spectacle.getAvailableTickets() + ", Demandés: " + request.getQuantity());
        }

        // Calculer le prix total
        BigDecimal totalPrice = spectacle.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        // Créer la réservation
        Reservation reservation = Reservation.builder()
                .keycloakUserId(keycloakUserId)
                .spectacle(spectacle)
                .quantity(request.getQuantity())
                .totalPrice(totalPrice)
                .reservationDate(LocalDateTime.now())
                .build();

        // Mettre à jour les billets disponibles
        spectacle.setAvailableTickets(spectacle.getAvailableTickets() - request.getQuantity());
        spectacleRepository.save(spectacle);

        // Sauvegarder la réservation
        reservation = reservationRepository.save(reservation);

        return mapToResponse(reservation);
    }

    public List<ReservationResponse> getUserReservations(String keycloakUserId) {
        List<Reservation> reservations = reservationRepository.findByKeycloakUserId(keycloakUserId);
        return reservations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ReservationResponse getReservationById(Long id, String keycloakUserId) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Réservation non trouvée avec l'ID: " + id));

        // Vérifier que la réservation appartient bien à l'utilisateur
        if (!reservation.getKeycloakUserId().equals(keycloakUserId)) {
            throw new SecurityException("Vous n'êtes pas autorisé à accéder à cette réservation");
        }

        return mapToResponse(reservation);
    }

    @Transactional
    public void cancelReservation(Long id, String keycloakUserId) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Réservation non trouvée avec l'ID: " + id));

        // Vérifier que la réservation appartient bien à l'utilisateur
        if (!reservation.getKeycloakUserId().equals(keycloakUserId)) {
            throw new SecurityException("Vous n'êtes pas autorisé à annuler cette réservation");
        }

        // Remettre les billets disponibles
        Spectacle spectacle = reservation.getSpectacle();
        spectacle.setAvailableTickets(spectacle.getAvailableTickets() + reservation.getQuantity());
        spectacleRepository.save(spectacle);

        // Supprimer la réservation
        reservationRepository.delete(reservation);
    }

    public StatsResponse getStatistics() {
        BigDecimal totalRevenue = reservationRepository.getTotalSales();
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        Long totalReservations = reservationRepository.count();

        List<StatsResponse.SpectacleSales> salesBySpectacle =
                reservationRepository.getSalesBySpectacle().stream()
                        .map(sales -> StatsResponse.SpectacleSales.builder()
                                .spectacleId(sales.getSpectacleId())
                                .title(sales.getTitle())
                                .ticketsSold(sales.getTicketsSold())
                                .revenue(sales.getRevenue())
                                .build())
                        .collect(Collectors.toList());

        return StatsResponse.builder()
                .totalRevenue(totalRevenue)
                .totalReservations(totalReservations)
                .salesBySpectacle(salesBySpectacle)
                .build();
    }

    private ReservationResponse mapToResponse(Reservation reservation) {
        Spectacle spectacle = reservation.getSpectacle();

        return ReservationResponse.builder()
                .id(reservation.getId())
                .reservationDate(reservation.getReservationDate())
                .quantity(reservation.getQuantity())
                .totalPrice(reservation.getTotalPrice())
                .spectacle(ReservationResponse.SpectacleInfo.builder()
                        .id(spectacle.getId())
                        .title(spectacle.getTitle())
                        .date(spectacle.getDate())
                        .price(spectacle.getPrice())
                        .imageUrl(spectacle.getImageUrl())
                        .build())
                .build();
    }
}

