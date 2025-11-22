package epsi.archiapp.backend.controller;

import epsi.archiapp.backend.config.swagger.CommonApiResponses.*;
import epsi.archiapp.backend.dto.StatsResponse;
import epsi.archiapp.backend.service.CacheManagementService;
import epsi.archiapp.backend.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin
@Tag(name = "Administration", description = "API d'administration et statistiques")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final ReservationService reservationService;
    private final CacheManagementService cacheManagementService;

    @Operation(
        summary = "Récupère les statistiques de vente",
        description = "Récupère les statistiques globales et par spectacle. Nécessite le rôle ADMIN."
    )
    @GetApiResponses
    @AdminApiResponses
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public StatsResponse getStatistics() {
        return reservationService.getStatistics();
    }

    @Operation(
        summary = "Vide tous les caches",
        description = "Supprime toutes les entrées de tous les caches. Nécessite le rôle ADMIN."
    )
    @DeleteApiResponses
    @AdminApiResponses
    @DeleteMapping("/cache")
    @PreAuthorize("hasRole('ADMIN')")
    public void clearAllCaches() {
        cacheManagementService.clearAllCaches();
    }

    @Operation(
        summary = "Vide un cache spécifique",
        description = "Supprime toutes les entrées d'un cache donné (spectacles, reservations, statistics). Nécessite le rôle ADMIN."
    )
    @DeleteApiResponses
    @AdminApiResponses
    @DeleteMapping("/cache/{cacheName}")
    @PreAuthorize("hasRole('ADMIN')")
    public void clearCache(@PathVariable String cacheName) {
        cacheManagementService.clearCache(cacheName);
    }

    @Operation(
        summary = "Liste les caches disponibles",
        description = "Retourne la liste des noms de caches configurés. Nécessite le rôle ADMIN."
    )
    @GetApiResponses
    @AdminApiResponses
    @GetMapping("/cache/names")
    @PreAuthorize("hasRole('ADMIN')")
    public java.util.Collection<String> getCacheNames() {
        return cacheManagementService.getCacheNames();
    }

    @Operation(
        summary = "Récupère les statistiques des caches",
        description = "Retourne les statistiques détaillées de tous les caches (taille, hits, miss, hit rate, evictions). Nécessite le rôle ADMIN."
    )
    @GetApiResponses
    @AdminApiResponses
    @GetMapping("/cache/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public java.util.Map<String, epsi.archiapp.backend.service.CacheManagementService.CacheStatisticsInfo> getCacheStatistics() {
        return cacheManagementService.getCacheStatistics();
    }
}

