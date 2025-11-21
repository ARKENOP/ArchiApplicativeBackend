package epsi.archiapp.backend.controller;

import epsi.archiapp.backend.config.swagger.CommonApiResponses.*;
import epsi.archiapp.backend.dto.SpectacleRequest;
import epsi.archiapp.backend.dto.SpectacleResponse;
import epsi.archiapp.backend.service.SpectacleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/spectacles")
@RequiredArgsConstructor
@CrossOrigin
@Slf4j
@Tag(name = "Spectacles", description = "API de gestion des spectacles")
public class SpectacleController {

    private final SpectacleService spectacleService;

    @Operation(
        summary = "Liste tous les spectacles",
        description = "Récupère la liste paginée de tous les spectacles disponibles. Accessible sans authentification."
    )
    @ListApiResponses
    @GetMapping
    public ResponseEntity<Page<SpectacleResponse>> list(
            @Parameter(description = "Paramètres de pagination et tri")
            @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("Récupération de la liste des spectacles - page: {}, taille: {}",
                 pageable.getPageNumber(), pageable.getPageSize());
        Page<SpectacleResponse> spectacles = spectacleService.listAll(pageable);
        return ResponseEntity.ok(spectacles);
    }

    @Operation(
        summary = "Récupère un spectacle par son ID",
        description = "Récupère les détails d'un spectacle spécifique. Accessible sans authentification."
    )
    @GetApiResponses
    @GetMapping("/{id}")
    public ResponseEntity<SpectacleResponse> get(
            @Parameter(description = "ID du spectacle", required = true)
            @PathVariable Long id) {
        log.info("Récupération du spectacle avec ID: {}", id);
        SpectacleResponse spectacle = spectacleService.get(id);
        return ResponseEntity.ok(spectacle);
    }

    @Operation(
        summary = "Crée un nouveau spectacle",
        description = "Crée un nouveau spectacle. Nécessite le rôle ADMIN.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @CreateApiResponses
    @AdminApiResponses
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SpectacleResponse> create(
            @Parameter(description = "Données du spectacle à créer", required = true)
            @Valid @RequestBody SpectacleRequest request) {
        log.info("Création d'un nouveau spectacle: {}", request.getTitle());
        SpectacleResponse created = spectacleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(
        summary = "Met à jour un spectacle existant",
        description = "Met à jour les informations d'un spectacle. Nécessite le rôle ADMIN.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @UpdateApiResponses
    @AdminApiResponses
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SpectacleResponse> update(
            @Parameter(description = "ID du spectacle à modifier", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nouvelles données du spectacle", required = true)
            @Valid @RequestBody SpectacleRequest request) {
        log.info("Mise à jour du spectacle avec ID: {}", id);
        SpectacleResponse updated = spectacleService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(
        summary = "Supprime un spectacle",
        description = "Supprime un spectacle existant. Nécessite le rôle ADMIN.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteApiResponses
    @AdminApiResponses
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID du spectacle à supprimer", required = true)
            @PathVariable Long id) {
        log.info("Suppression du spectacle avec ID: {}", id);
        spectacleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
