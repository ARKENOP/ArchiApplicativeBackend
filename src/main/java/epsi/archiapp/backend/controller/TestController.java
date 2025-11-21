package epsi.archiapp.backend.controller;

import epsi.archiapp.backend.dto.ApiInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Tag(name = "Utilitaires", description = "Endpoints utilitaires et d'information")
public class TestController {

    @Value("${info.app.name}")
    private String appName;

    @Value("${info.app.description}")
    private String description;

    @Value("${info.app.version:1.0.0}")
    private String appVersion;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Operation(
        summary = "Informations sur l'API",
        description = "Récupère les informations générales sur l'API (accessible sans authentification)"
    )
    @ApiResponse(responseCode = "200", description = "Informations récupérées avec succès")
    @GetMapping("/info")
    public ResponseEntity<ApiInfoResponse> getInfo() {
        ApiInfoResponse info = ApiInfoResponse.builder()
                .name(appName)
                .version(appVersion)
                .description(description)
                .environment(activeProfile)
                .build();
        return ResponseEntity.ok(info);
    }

    @Operation(
        summary = "Test d'authentification",
        description = "Endpoint de test pour vérifier l'authentification",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Utilisateur authentifié")
    @ApiResponse(responseCode = "401", description = "Non authentifié")
    @GetMapping("/test/hello")
    public ResponseEntity<Map<String, Object>> helloWorld(
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello World! You are authenticated.");
        response.put("username", jwt.getClaimAsString("preferred_username"));
        response.put("userId", jwt.getSubject());
        response.put("email", jwt.getClaimAsString("email"));
        response.put("roles", authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Health check",
        description = "Endpoint simple de health check (accessible sans authentification)"
    )
    @ApiResponse(responseCode = "200", description = "Service opérationnel")
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "API Réservation Théâtre");
        return ResponseEntity.ok(status);
    }
}
