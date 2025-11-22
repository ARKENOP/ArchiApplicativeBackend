package epsi.archiapp.backend.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Tests de l'utilitaire JWT")
class JwtUtilsTest {

    @Test
    @DisplayName("Doit extraire l'userId du claim 'sub'")
    void testExtractUserIdFromSub() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user-123");
        Jwt jwt = createJwt(claims);

        // When
        String userId = JwtUtils.extractUserId(jwt);

        // Then
        assertThat(userId).isEqualTo("user-123");
    }

    @Test
    @DisplayName("Doit extraire l'userId du claim 'preferred_username' si 'sub' est vide")
    void testExtractUserIdFromPreferredUsername() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "");
        claims.put("preferred_username", "john.doe");
        Jwt jwt = createJwt(claims);

        // When
        String userId = JwtUtils.extractUserId(jwt);

        // Then
        assertThat(userId).isEqualTo("john.doe");
    }

    @Test
    @DisplayName("Doit extraire l'userId du claim 'email' si 'sub' et 'preferred_username' sont vides")
    void testExtractUserIdFromEmail() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "");
        claims.put("preferred_username", "");
        claims.put("email", "john.doe@example.com");
        Jwt jwt = createJwt(claims);

        // When
        String userId = JwtUtils.extractUserId(jwt);

        // Then
        assertThat(userId).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("Doit lancer IllegalArgumentException si le JWT est null")
    void testExtractUserIdNullJwt() {
        // When & Then
        assertThatThrownBy(() -> JwtUtils.extractUserId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JWT token est null");
    }

    @Test
    @DisplayName("Doit lancer IllegalArgumentException si aucun claim d'identification n'est présent")
    void testExtractUserIdNoIdentificationClaim() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("some_other_claim", "value");
        Jwt jwt = createJwt(claims);

        // When & Then
        assertThatThrownBy(() -> JwtUtils.extractUserId(jwt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Impossible d'extraire l'identifiant utilisateur");
    }

    @Test
    @DisplayName("Doit gérer les claims avec des espaces")
    void testExtractUserIdWithWhitespace() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "   ");
        claims.put("preferred_username", "john.doe");
        Jwt jwt = createJwt(claims);

        // When
        String userId = JwtUtils.extractUserId(jwt);

        // Then
        assertThat(userId).isEqualTo("john.doe");
    }

    /**
     * Méthode utilitaire pour créer un JWT de test
     */
    private Jwt createJwt(Map<String, Object> claims) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .claims(c -> c.putAll(claims))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }
}

