package epsi.archiapp.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import epsi.archiapp.backend.config.SecurityConfig;
import epsi.archiapp.backend.dto.SpectacleRequest;
import epsi.archiapp.backend.dto.SpectacleResponse;
import epsi.archiapp.backend.service.SpectacleService;
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

@WebMvcTest(SpectacleController.class)
@Import(SecurityConfig.class)
class SpectacleControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SpectacleService spectacleService;
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void list_shouldBePublic() throws Exception {
        SpectacleResponse response = SpectacleResponse.builder().id(1L).title("Hamlet").build();
        when(spectacleService.listAll(any())).thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/spectacles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Hamlet"));
    }

    @Test
    void create_shouldReturn401WithoutAuthentication() throws Exception {
        SpectacleRequest request = validRequest();

        mockMvc.perform(post("/api/spectacles")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_shouldReturn403ForNonAdmin() throws Exception {
        SpectacleRequest request = validRequest();

        mockMvc.perform(post("/api/spectacles")
                        .with(JwtTestFactory.userJwt("user"))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_shouldReturn201ForAdmin() throws Exception {
        SpectacleRequest request = validRequest();
        SpectacleResponse response = SpectacleResponse.builder().id(3L).title("Nouveau").build();
        when(spectacleService.create(any(SpectacleRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/spectacles")
                        .with(JwtTestFactory.adminJwt("admin"))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    void update_shouldReturn200ForAdmin() throws Exception {
        SpectacleRequest request = validRequest();
        SpectacleResponse response = SpectacleResponse.builder().id(7L).title("Maj").build();
        when(spectacleService.update(eq(7L), any(SpectacleRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/spectacles/7")
                        .with(JwtTestFactory.adminJwt("admin"))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Maj"));
    }

    @Test
    void delete_shouldReturn204ForAdmin() throws Exception {
        mockMvc.perform(delete("/api/spectacles/9").with(JwtTestFactory.adminJwt("admin")))
                .andExpect(status().isNoContent());

        verify(spectacleService).delete(9L);
    }

    private SpectacleRequest validRequest() {
        return SpectacleRequest.builder()
                .title("Nouveau")
                .description("Description")
                .date(LocalDateTime.now().plusDays(3))
                .price(new BigDecimal("22.00"))
                .availableTickets(50)
                .imageUrl("https://example.test/img.jpg")
                .build();
    }
}


