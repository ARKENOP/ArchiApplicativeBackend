package epsi.archiapp.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import epsi.archiapp.backend.config.SecurityConfig;
import epsi.archiapp.backend.dto.ReservationRequest;
import epsi.archiapp.backend.dto.ReservationResponse;
import epsi.archiapp.backend.exception.UnauthorizedAccessException;
import epsi.archiapp.backend.service.ReservationService;
import epsi.archiapp.backend.testsupport.JwtTestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
@Import(SecurityConfig.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void createReservation_shouldReturn401WithoutAuthentication() throws Exception {
        ReservationRequest request = new ReservationRequest(1L, 2);

        mockMvc.perform(post("/api/reservations")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createReservation_shouldReturn201WithAuthenticatedUser() throws Exception {
        ReservationRequest request = new ReservationRequest(1L, 2);
        ReservationResponse response = ReservationResponse.builder()
                .id(10L)
                .quantity(2)
                .totalPrice(new BigDecimal("60.00"))
                .reservationDate(LocalDateTime.now())
                .build();

        when(reservationService.createReservation(eq("user-1"), any(ReservationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/reservations")
                        .with(JwtTestFactory.userJwt("user-1"))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.quantity").value(2));

        verify(reservationService).createReservation(eq("user-1"), any(ReservationRequest.class));
    }

    @Test
    void createReservation_shouldReturn400WhenValidationFails() throws Exception {
        ReservationRequest invalid = new ReservationRequest(1L, 0);

        mockMvc.perform(post("/api/reservations")
                        .with(JwtTestFactory.userJwt("user-1"))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Erreur de validation"));
    }

    @Test
    void getUserReservations_shouldReturnPagedData() throws Exception {
        ReservationResponse response = ReservationResponse.builder().id(1L).quantity(1).build();
        when(reservationService.getUserReservations(eq("user-1"), any()))
                .thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/reservations").with(JwtTestFactory.userJwt("user-1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void getReservation_shouldReturn403WhenServiceThrowsUnauthorizedAccess() throws Exception {
        when(reservationService.getReservationById(8L, "user-1"))
                .thenThrow(new UnauthorizedAccessException("cette réservation", 8L));

        mockMvc.perform(get("/api/reservations/8").with(JwtTestFactory.userJwt("user-1")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void cancelReservation_shouldReturn204ForAuthenticatedUser() throws Exception {
        mockMvc.perform(delete("/api/reservations/2").with(JwtTestFactory.userJwt("user-1")))
                .andExpect(status().isNoContent());

        verify(reservationService).cancelReservation(2L, "user-1");
    }
}


