package epsi.archiapp.backend.util;

import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Utilitaire pour extraire l'identifiant utilisateur depuis un token JWT Keycloak
 */
public class JwtUtils {

    /**
     * Extrait l'identifiant utilisateur du token JWT.
     *
     * Note: Le token Keycloak configuré n'inclut pas le claim 'sub' standard.
     * On utilise donc 'preferred_username' comme identifiant utilisateur.
     *
     * @param jwt Le token JWT
     * @return L'identifiant utilisateur (preferred_username)
     * @throws IllegalArgumentException si le JWT est null ou ne contient pas d'identifiant
     */
    public static String extractUserId(Jwt jwt) {
        if (jwt == null) {
            throw new IllegalArgumentException("JWT token est null");
        }

        // Essayer d'abord avec 'sub' (standard OAuth2)
        String userId = jwt.getSubject();

        // Si 'sub' n'existe pas, utiliser 'preferred_username' (Keycloak)
        if (userId == null || userId.trim().isEmpty()) {
            userId = jwt.getClaim("preferred_username");
        }

        // Si toujours null, essayer 'email' comme fallback
        if (userId == null || userId.trim().isEmpty()) {
            userId = jwt.getClaim("email");
        }

        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("Impossible d'extraire l'identifiant utilisateur du token JWT. " +
                    "Claims disponibles: " + jwt.getClaims().keySet());
        }

        return userId;
    }

    /**
     * Extrait le nom complet de l'utilisateur
     */
    public static String extractUserName(Jwt jwt) {
        if (jwt == null) return null;

        String name = jwt.getClaim("name");
        if (name != null && !name.trim().isEmpty()) {
            return name;
        }

        return jwt.getClaim("preferred_username");
    }

    /**
     * Extrait l'email de l'utilisateur
     */
    public static String extractUserEmail(Jwt jwt) {
        if (jwt == null) return null;
        return jwt.getClaim("email");
    }
}

