package epsi.archiapp.backend.controller;

import epsi.archiapp.backend.dto.ReservationRequest;
import epsi.archiapp.backend.dto.ReservationResponse;
import epsi.archiapp.backend.service.ReservationService;
import epsi.archiapp.backend.util.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@CrossOrigin
@Slf4j
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse createReservation(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ReservationRequest request) {
        String userId = JwtUtils.extractUserId(jwt);
        log.info("Création de réservation pour l'utilisateur: {}", userId);
        return reservationService.createReservation(userId, request);
    }

    @GetMapping
    public List<ReservationResponse> getUserReservations(@AuthenticationPrincipal Jwt jwt) {
        String userId = JwtUtils.extractUserId(jwt);
        return reservationService.getUserReservations(userId);
    }

    @GetMapping("/{id}")
    public ReservationResponse getReservation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {
        String userId = JwtUtils.extractUserId(jwt);
        return reservationService.getReservationById(id, userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelReservation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {
        String userId = JwtUtils.extractUserId(jwt);
        reservationService.cancelReservation(id, userId);
    }
}

