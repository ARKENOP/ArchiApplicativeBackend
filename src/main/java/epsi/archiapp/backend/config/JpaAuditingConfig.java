package epsi.archiapp.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

/**
 * Configuration de l'auditing JPA
 * Permet le remplissage automatique des champs createdAt, updatedAt, createdBy, updatedBy
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    /**
     * Fournit l'auditeur actuel (utilisateur connecté ou "system")
     * - Si un utilisateur est authentifié : retourne son ID Keycloak
     * - Sinon : retourne "system" (pour l'initialisation des données, migrations, etc.)
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditorAwareImpl();
    }

    /**
     * Implémentation de AuditorAware pour récupérer l'utilisateur actuel
     */
    static class AuditorAwareImpl implements AuditorAware<String> {

        @Override
        @NonNull
        public Optional<String> getCurrentAuditor() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Si pas d'authentification ou utilisateur anonyme
            if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.of("system");
            }

            // Si l'utilisateur est authentifié via JWT (Keycloak)
            if (authentication.getPrincipal() instanceof Jwt jwt) {
                // Récupérer le username depuis le token JWT
                String username = jwt.getClaimAsString("preferred_username");

                return Optional.ofNullable(username);
            }

            // Fallback : utiliser le nom du principal
            return Optional.of(authentication.getName());
        }
    }
}

