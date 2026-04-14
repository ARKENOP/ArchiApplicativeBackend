package epsi.archiapp.backend.controller;

import epsi.archiapp.backend.config.SecurityConfig;
import epsi.archiapp.backend.testsupport.JwtTestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TestController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "info.app.name=Billetterie Test",
        "info.app.description=API test",
        "info.app.version=9.9.9",
        "spring.profiles.active=test"
})
class TestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void getInfo_shouldBePublic() throws Exception {
        mockMvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Billetterie Test"))
                .andExpect(jsonPath("$.version").value("9.9.9"));
    }

    @Test
    void health_shouldBePublic() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void hello_shouldReturn401WithoutJwt() throws Exception {
        mockMvc.perform(get("/api/test/hello"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void hello_shouldReturnJwtClaimsForAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/test/hello").with(JwtTestFactory.userJwt("paul")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("paul"))
                .andExpect(jsonPath("$.userId").value("paul"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }
}


