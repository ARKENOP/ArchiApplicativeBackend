package epsi.archiapp.backend.controller;

import epsi.archiapp.backend.config.SecurityConfig;
import epsi.archiapp.backend.dto.StatsResponse;
import epsi.archiapp.backend.service.CacheManagementService;
import epsi.archiapp.backend.service.ReservationService;
import epsi.archiapp.backend.testsupport.JwtTestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;
    @MockitoBean
    private CacheManagementService cacheManagementService;
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void getStats_shouldReturn401WithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getStats_shouldReturn403ForNonAdminUser() throws Exception {
        mockMvc.perform(get("/api/admin/stats").with(JwtTestFactory.userJwt("paul")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getStats_shouldReturnPayloadForAdmin() throws Exception {
        StatsResponse response = StatsResponse.builder()
                .totalRevenue(new BigDecimal("123.45"))
                .totalReservations(6L)
                .salesBySpectacle(List.of())
                .build();
        when(reservationService.getStatistics()).thenReturn(response);

        mockMvc.perform(get("/api/admin/stats").with(JwtTestFactory.adminJwt("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").value(123.45))
                .andExpect(jsonPath("$.totalReservations").value(6));
    }

    @Test
    void clearCache_shouldCallServiceForAdmin() throws Exception {
        mockMvc.perform(delete("/api/admin/cache/spectacles").with(JwtTestFactory.adminJwt("admin")))
                .andExpect(status().isOk());

        verify(cacheManagementService).clearCache("spectacles");
    }

    @Test
    void getCacheNames_shouldReturnConfiguredNames() throws Exception {
        when(cacheManagementService.getCacheNames()).thenReturn(List.of("spectacles", "reservations"));

        mockMvc.perform(get("/api/admin/cache/names").with(JwtTestFactory.adminJwt("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("spectacles"))
                .andExpect(jsonPath("$[1]").value("reservations"));
    }

    @Test
    void getCacheStatistics_shouldReturnDetails() throws Exception {
        CacheManagementService.CacheStatisticsInfo info = new CacheManagementService.CacheStatisticsInfo(
                "spectacles", 10, 4, 1, 0.8, 0
        );
        when(cacheManagementService.getCacheStatistics()).thenReturn(Map.of("spectacles", info));

        mockMvc.perform(get("/api/admin/cache/statistics").with(JwtTestFactory.adminJwt("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spectacles.name").value("spectacles"))
                .andExpect(jsonPath("$.spectacles.size").value(10));
    }
}


