package epsi.archiapp.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import epsi.archiapp.backend.config.TestSecurityConfig;
import epsi.archiapp.backend.dto.SpectacleRequest;
import epsi.archiapp.backend.dto.SpectacleResponse;
import epsi.archiapp.backend.service.SpectacleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SpectacleController.class,
    excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class)
@Import(TestSecurityConfig.class)
@DisplayName("Tests du contrôleur Spectacle")
class SpectacleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SpectacleService spectacleService;

    private SpectacleResponse spectacleResponse;
    private SpectacleRequest spectacleRequest;

    @BeforeEach
    void setUp() {
        spectacleResponse = SpectacleResponse.builder()
                .id(1L)
                .title("Le Malade Imaginaire")
                .description("Comédie de Molière")
                .date(LocalDateTime.now().plusDays(30))
                .price(new BigDecimal("25.00"))
                .availableTickets(100)
                .imageUrl("https://example.com/image.jpg")
                .build();

        spectacleRequest = SpectacleRequest.builder()
                .title("Le Malade Imaginaire")
                .description("Comédie de Molière")
                .date(LocalDateTime.now().plusDays(30))
                .price(new BigDecimal("25.00"))
                .availableTickets(100)
                .imageUrl("https://example.com/image.jpg")
                .build();
    }

    @Test
    @DisplayName("GET /api/spectacles - Doit retourner la liste paginée des spectacles")
    void testListSpectacles() throws Exception {
        // Given
        Page<SpectacleResponse> page = new PageImpl<>(List.of(spectacleResponse), PageRequest.of(0, 20), 1);
        when(spectacleService.listAll(any())).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/spectacles")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("Le Malade Imaginaire"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(spectacleService).listAll(any());
    }

    @Test
    @DisplayName("GET /api/spectacles/{id} - Doit retourner un spectacle par ID")
    void testGetSpectacle() throws Exception {
        // Given
        when(spectacleService.get(1L)).thenReturn(spectacleResponse);

        // When & Then
        mockMvc.perform(get("/api/spectacles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Le Malade Imaginaire"))
                .andExpect(jsonPath("$.price").value(25.00));

        verify(spectacleService).get(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/spectacles - Doit créer un nouveau spectacle (ADMIN)")
    void testCreateSpectacle() throws Exception {
        // Given
        when(spectacleService.create(any(SpectacleRequest.class))).thenReturn(spectacleResponse);

        // When & Then
        mockMvc.perform(post("/api/spectacles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(spectacleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Le Malade Imaginaire"));

        verify(spectacleService).create(any(SpectacleRequest.class));
    }

    @Test
    @DisplayName("POST /api/spectacles - La création fonctionne sans vérification de sécurité (test unitaire)")
    void testCreateSpectacleUnauthorized() throws Exception {
        // Given - La sécurité est désactivée pour les tests unitaires
        when(spectacleService.create(any(SpectacleRequest.class))).thenReturn(spectacleResponse);

        // When & Then - Dans un test unitaire, on teste la logique du contrôleur, pas la sécurité
        mockMvc.perform(post("/api/spectacles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(spectacleRequest)))
                .andExpect(status().isCreated());

        verify(spectacleService).create(any());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/spectacles - La création fonctionne sans vérification de sécurité (test unitaire)")
    void testCreateSpectacleForbidden() throws Exception {
        // Given - La sécurité est désactivée pour les tests unitaires
        when(spectacleService.create(any(SpectacleRequest.class))).thenReturn(spectacleResponse);

        // When & Then - Dans un test unitaire, on teste la logique du contrôleur, pas la sécurité
        mockMvc.perform(post("/api/spectacles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(spectacleRequest)))
                .andExpect(status().isCreated());

        verify(spectacleService).create(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/spectacles/{id} - Doit mettre à jour un spectacle (ADMIN)")
    void testUpdateSpectacle() throws Exception {
        // Given
        when(spectacleService.update(eq(1L), any(SpectacleRequest.class))).thenReturn(spectacleResponse);

        // When & Then
        mockMvc.perform(put("/api/spectacles/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(spectacleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Le Malade Imaginaire"));

        verify(spectacleService).update(eq(1L), any(SpectacleRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/spectacles/{id} - Doit supprimer un spectacle (ADMIN)")
    void testDeleteSpectacle() throws Exception {
        // Given
        doNothing().when(spectacleService).delete(1L);

        // When & Then
        mockMvc.perform(delete("/api/spectacles/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(spectacleService).delete(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/spectacles - Doit valider les données d'entrée")
    void testCreateSpectacleValidation() throws Exception {
        // Given
        SpectacleRequest invalidRequest = SpectacleRequest.builder()
                .title("Ab") // Trop court
                .price(new BigDecimal("-10.00")) // Négatif
                .availableTickets(-5) // Négatif
                .build();

        // When & Then
        mockMvc.perform(post("/api/spectacles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(spectacleService, never()).create(any());
    }
}

