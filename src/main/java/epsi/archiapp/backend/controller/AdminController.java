package epsi.archiapp.backend.controller;

import epsi.archiapp.backend.config.swagger.CommonApiResponses.*;
import epsi.archiapp.backend.dto.StatsResponse;
import epsi.archiapp.backend.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
@CrossOrigin
@Tag(name = "Administration", description = "API d'administration et statistiques")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final ReservationService reservationService;

    @Operation(
        summary = "Récupère les statistiques de vente",
        description = "Récupère les statistiques globales et par spectacle. Nécessite le rôle ADMIN."
    )
    @GetApiResponses
    @AdminApiResponses
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public StatsResponse getStatistics() {
        return reservationService.getStatistics();
    }
}

