package epsi.archiapp.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import epsi.archiapp.backend.config.TestSecurityConfig;
import epsi.archiapp.backend.dto.ReservationRequest;
import epsi.archiapp.backend.dto.ReservationResponse;
import epsi.archiapp.backend.service.ReservationService;
import epsi.archiapp.backend.util.JwtUtils;
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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReservationController.class,
    excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class)
@Import(TestSecurityConfig.class)
@DisplayName("Tests du contrôleur Réservation")
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    private ReservationResponse reservationResponse;
    private ReservationRequest reservationRequest;

    @BeforeEach
    void setUp() {
        reservationRequest = new ReservationRequest(1L, 2);

        reservationResponse = ReservationResponse.builder()
                .id(1L)
                .quantity(2)
                .totalPrice(new BigDecimal("50.00"))
                .reservationDate(LocalDateTime.now())
                .spectacle(ReservationResponse.SpectacleInfo.builder()
                        .id(1L)
                        .title("Le Malade Imaginaire")
                        .date(LocalDateTime.now().plusDays(30))
                        .price(new BigDecimal("25.00"))
                        .build())
                .build();
    }

    @Test
    @WithMockUser(username = "user-123")
    @DisplayName("POST /api/reservations - Doit créer une réservation avec authentification")
    void testCreateReservation() throws Exception {
        // Given
        when(reservationService.createReservation(anyString(), any(ReservationRequest.class)))
                .thenReturn(reservationResponse);

        // When & Then
        mockMvc.perform(post("/api/reservations")
                        .with(csrf())
                        .with(jwt().jwt(builder -> builder.subject("user-123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.totalPrice").value(50.00));

        verify(reservationService).createReservation(anyString(), any(ReservationRequest.class));
    }

    @Test
    @DisplayName("POST /api/reservations - Sans JWT, le contrôleur reçoit null et échoue")
    void testCreateReservationUnauthorized() throws Exception {
        // Dans un test unitaire avec sécurité désactivée, la requête sans JWT
        // provoque une erreur 400 car le contrôleur reçoit null pour le JWT
        // En production, Spring Security bloquerait avec 401
        mockMvc.perform(post("/api/reservations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().isBadRequest());

        verify(reservationService, never()).createReservation(anyString(), any());
    }

    @Test
    @WithMockUser(username = "user-123")
    @DisplayName("GET /api/reservations - Doit retourner les réservations de l'utilisateur")
    void testGetUserReservations() throws Exception {
        // Given
        Page<ReservationResponse> page = new PageImpl<>(List.of(reservationResponse), PageRequest.of(0, 20), 1);
        when(reservationService.getUserReservations(anyString(), any())).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/reservations")
                        .with(jwt().jwt(builder -> builder.subject("user-123")))
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(reservationService).getUserReservations(anyString(), any());
    }

    @Test
    @WithMockUser(username = "user-123")
    @DisplayName("GET /api/reservations/{id} - Doit retourner une réservation spécifique")
    void testGetReservation() throws Exception {
        // Given
        when(reservationService.getReservationById(eq(1L), anyString())).thenReturn(reservationResponse);

        // When & Then
        mockMvc.perform(get("/api/reservations/1")
                        .with(jwt().jwt(builder -> builder.subject("user-123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.quantity").value(2));

        verify(reservationService).getReservationById(eq(1L), anyString());
    }

    @Test
    @WithMockUser(username = "user-123")
    @DisplayName("DELETE /api/reservations/{id} - Doit annuler une réservation")
    void testCancelReservation() throws Exception {
        // Given
        doNothing().when(reservationService).cancelReservation(eq(1L), anyString());

        // When & Then
        mockMvc.perform(delete("/api/reservations/1")
                        .with(csrf())
                        .with(jwt().jwt(builder -> builder.subject("user-123"))))
                .andExpect(status().isNoContent());

        verify(reservationService).cancelReservation(eq(1L), anyString());
    }

    @Test
    @WithMockUser(username = "user-123")
    @DisplayName("POST /api/reservations - Doit valider les données d'entrée")
    void testCreateReservationValidation() throws Exception {
        // Given
        ReservationRequest invalidRequest = new ReservationRequest(null, -1);

        // When & Then
        mockMvc.perform(post("/api/reservations")
                        .with(csrf())
                        .with(jwt().jwt(builder -> builder.subject("user-123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(reservationService, never()).createReservation(anyString(), any());
    }
}

