package epsi.archiapp.backend.controller;

import epsi.archiapp.backend.config.swagger.CommonApiResponses.*;
import epsi.archiapp.backend.dto.ReservationRequest;
import epsi.archiapp.backend.dto.ReservationResponse;
import epsi.archiapp.backend.service.ReservationService;
import epsi.archiapp.backend.util.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@CrossOrigin
@Slf4j
@Tag(name = "Réservations", description = "API de gestion des réservations de billets")
@SecurityRequirement(name = "bearerAuth")
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(
        summary = "Crée une nouvelle réservation",
        description = "Permet à un utilisateur authentifié de créer une réservation pour un spectacle. " +
                     "Le système vérifie automatiquement la disponibilité des billets."
    )
    @CreateApiResponses
    @ApiResponses(value = {
        @ApiResponse(responseCode = "404", description = "Spectacle non trouvé"),
        @ApiResponse(responseCode = "409", description = "Billets insuffisants pour ce spectacle")
    })
    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Détails de la réservation", required = true)
            @Valid @RequestBody ReservationRequest request) {
        String userId = JwtUtils.extractUserId(jwt);
        log.info("Création de réservation pour l'utilisateur: {} - Spectacle: {}, Quantité: {}",
                 userId, request.getSpectacleId(), request.getQuantity());
        ReservationResponse reservation = reservationService.createReservation(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
    }

    @Operation(
        summary = "Liste les réservations de l'utilisateur",
        description = "Récupère toutes les réservations effectuées par l'utilisateur authentifié, " +
                     "triées par date de réservation décroissante."
    )
    @ListApiResponses
    @AuthApiResponses
    @GetMapping
    public ResponseEntity<Page<ReservationResponse>> getUserReservations(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Paramètres de pagination et tri")
            @PageableDefault(size = 20, sort = "reservationDate", direction = Sort.Direction.DESC) Pageable pageable) {
        String userId = JwtUtils.extractUserId(jwt);
        log.info("Récupération des réservations pour l'utilisateur: {} - page: {}, taille: {}",
                userId, pageable.getPageNumber(), pageable.getPageSize());
        Page<ReservationResponse> reservations = reservationService.getUserReservations(userId, pageable);
        return ResponseEntity.ok(reservations);
    }

    @Operation(
        summary = "Récupère une réservation spécifique",
        description = "Récupère les détails d'une réservation. L'utilisateur ne peut accéder qu'à ses propres réservations."
    )
    @GetApiResponses
    @AuthApiResponses
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getReservation(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID de la réservation", required = true)
            @PathVariable Long id) {
        String userId = JwtUtils.extractUserId(jwt);
        log.info("Récupération de la réservation {} pour l'utilisateur: {}", id, userId);
        ReservationResponse reservation = reservationService.getReservationById(id, userId);
        return ResponseEntity.ok(reservation);
    }

    @Operation(
        summary = "Annule une réservation",
        description = "Annule une réservation existante et remet les billets en disponibilité. " +
                     "L'utilisateur ne peut annuler que ses propres réservations."
    )
    @DeleteApiResponses
    @AuthApiResponses
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelReservation(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID de la réservation à annuler", required = true)
            @PathVariable Long id) {
        String userId = JwtUtils.extractUserId(jwt);
        log.info("Annulation de la réservation {} par l'utilisateur: {}", id, userId);
        reservationService.cancelReservation(id, userId);
        return ResponseEntity.noContent().build();
    }
}

