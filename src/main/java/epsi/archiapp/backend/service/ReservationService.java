package epsi.archiapp.backend.service;

import epsi.archiapp.backend.dto.ReservationRequest;
import epsi.archiapp.backend.dto.ReservationResponse;
import epsi.archiapp.backend.dto.StatsResponse;
import epsi.archiapp.backend.exception.InsufficientTicketsException;
import epsi.archiapp.backend.exception.ResourceNotFoundException;
import epsi.archiapp.backend.exception.UnauthorizedAccessException;
import epsi.archiapp.backend.mapper.ReservationMapper;
import epsi.archiapp.backend.model.Reservation;
import epsi.archiapp.backend.model.Spectacle;
import epsi.archiapp.backend.repository.ReservationRepository;
import epsi.archiapp.backend.repository.SpectacleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final SpectacleRepository spectacleRepository;
    private final ReservationMapper reservationMapper;

    @Transactional
    public ReservationResponse createReservation(String keycloakUserId, ReservationRequest request) {
        log.info("Création de réservation - Utilisateur: {}, Spectacle: {}, Quantité: {}",
                 keycloakUserId, request.getSpectacleId(), request.getQuantity());

        // Récupérer le spectacle avec verrouillage pessimiste
        Spectacle spectacle = spectacleRepository.findById(request.getSpectacleId())
                .orElseThrow(() -> new ResourceNotFoundException("Spectacle", "id", request.getSpectacleId()));

        // Vérifier la disponibilité
        if (spectacle.getAvailableTickets() < request.getQuantity()) {
            log.warn("Billets insuffisants - Disponibles: {}, Demandés: {}",
                     spectacle.getAvailableTickets(), request.getQuantity());
            throw new InsufficientTicketsException(spectacle.getAvailableTickets(), request.getQuantity());
        }

        // Vérifier que le spectacle est dans le futur
        if (spectacle.getDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Impossible de réserver un spectacle passé");
        }

        // Calculer le prix total
        BigDecimal totalPrice = spectacle.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        // Créer la réservation
        Reservation reservation = reservationMapper.toEntity(request, spectacle, keycloakUserId, totalPrice);

        // Mettre à jour les billets disponibles
        spectacle.setAvailableTickets(spectacle.getAvailableTickets() - request.getQuantity());
        spectacleRepository.save(spectacle);

        // Sauvegarder la réservation
        reservation = reservationRepository.save(reservation);

        log.info("Réservation créée avec succès - ID: {}, Montant: {}",
                 reservation.getId(), totalPrice);

        return reservationMapper.toResponse(reservation);
    }

    @Transactional(readOnly = true)
    public Page<ReservationResponse> getUserReservations(String keycloakUserId, Pageable pageable) {
        log.debug("Récupération des réservations pour l'utilisateur: {} - page: {}",
                keycloakUserId, pageable.getPageNumber());
        return reservationRepository.findByKeycloakUserId(keycloakUserId, pageable)
                .map(reservationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id, String keycloakUserId) {
        log.debug("Récupération de la réservation {} pour l'utilisateur: {}", id, keycloakUserId);
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation", "id", id));

        // Vérifier que la réservation appartient bien à l'utilisateur
        if (!reservation.getKeycloakUserId().equals(keycloakUserId)) {
            log.warn("Tentative d'accès non autorisé à la réservation {} par l'utilisateur {}",
                     id, keycloakUserId);
            throw new UnauthorizedAccessException("cette réservation", id);
        }

        return reservationMapper.toResponse(reservation);
    }

    @Transactional
    public void cancelReservation(Long id, String keycloakUserId) {
        log.info("Annulation de la réservation {} par l'utilisateur: {}", id, keycloakUserId);
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation", "id", id));

        // Vérifier que la réservation appartient bien à l'utilisateur
        if (!reservation.getKeycloakUserId().equals(keycloakUserId)) {
            log.warn("Tentative d'annulation non autorisée de la réservation {} par l'utilisateur {}",
                     id, keycloakUserId);
            throw new UnauthorizedAccessException("cette réservation", id);
        }

        // Vérifier que le spectacle n'est pas déjà passé
        if (reservation.getSpectacle().getDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Impossible d'annuler une réservation pour un spectacle passé");
        }

        // Remettre les billets disponibles
        Spectacle spectacle = reservation.getSpectacle();
        spectacle.setAvailableTickets(spectacle.getAvailableTickets() + reservation.getQuantity());
        spectacleRepository.save(spectacle);

        // Supprimer la réservation
        reservationRepository.delete(reservation);

        log.info("Réservation annulée avec succès - ID: {}", id);
    }

    @Transactional(readOnly = true)
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

        log.debug("Statistiques calculées - Revenus: {}, Réservations: {}",
                  totalRevenue, totalReservations);

        return StatsResponse.builder()
                .totalRevenue(totalRevenue)
                .totalReservations(totalReservations)
                .salesBySpectacle(salesBySpectacle)
                .build();
    }
}

