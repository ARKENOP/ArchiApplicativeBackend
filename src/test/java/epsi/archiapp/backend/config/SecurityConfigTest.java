package epsi.archiapp.backend.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Tests de la configuration de sécurité")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Les endpoints publics doivent être accessibles sans authentification")
    void testPublicEndpoints() throws Exception {
        // Routes publiques - En environnement de test, peuvent rencontrer des erreurs de configuration OAuth2
        // Nous testons que les routes sont bien définies
        mockMvc.perform(get("/api/info"))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(get("/api/health"))
                .andExpect(status().is2xxSuccessful());

        // Swagger UI - peut rediriger ou retourner une erreur selon la config
        // On ne teste pas ces endpoints dans les tests de sécurité basiques
    }

    @Test
    @DisplayName("GET /api/spectacles doit être accessible sans authentification")
    void testSpectaclesPublicAccess() throws Exception {
        mockMvc.perform(get("/api/spectacles"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/spectacles doit nécessiter une authentification ADMIN")
    void testSpectaclesCreateRequiresAdmin() throws Exception {
        // Sans authentification
        mockMvc.perform(post("/api/spectacles").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/spectacles doit être refusé pour un utilisateur normal")
    void testSpectaclesCreateForbiddenForUser() throws Exception {
        mockMvc.perform(post("/api/spectacles").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/spectacles doit être autorisé pour un ADMIN")
    void testSpectaclesCreateAllowedForAdmin() throws Exception {
        // L'endpoint va échouer à cause de la validation, mais l'authentification passe
        mockMvc.perform(post("/api/spectacles")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest()); // Bad request à cause de la validation, pas de 403
    }

    @Test
    @DisplayName("Les réservations doivent nécessiter une authentification")
    void testReservationsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/reservations").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Les utilisateurs authentifiés peuvent accéder aux réservations")
    void testAuthenticatedUserCanAccessReservations() throws Exception {
        // Créer un mock JWT simple
        org.springframework.security.oauth2.jwt.Jwt jwt = org.springframework.security.oauth2.jwt.Jwt
                .withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "testuser")
                .build();

        // L'endpoint retourne une liste mais peut nécessiter un JWT valide
        mockMvc.perform(get("/api/reservations")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(jwt)))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("Les routes admin doivent nécessiter le rôle ADMIN")
    void testAdminRoutesRequireAdminRole() throws Exception {
        // Sans authentification
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Les routes admin doivent être interdites aux utilisateurs normaux")
    void testAdminRoutesForbiddenForUsers() throws Exception {
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Les routes admin doivent être accessibles aux ADMIN")
    void testAdminRoutesAllowedForAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("OPTIONS requests doivent être autorisées (CORS preflight)")
    void testOptionsRequestsAllowed() throws Exception {
        mockMvc.perform(options("/api/spectacles"))
                .andExpect(status().isOk());
    }
}

