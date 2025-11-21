package epsi.archiapp.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("API Réservation Théâtre")
                        .version("1.0.0")
                        .description("API REST pour la gestion des réservations de spectacles de théâtre.\n\n" +
                                "Cette API permet aux utilisateurs de consulter les spectacles disponibles, " +
                                "de créer des réservations et de consulter leurs achats. " +
                                "Les administrateurs peuvent gérer le catalogue de spectacles et consulter les statistiques de ventes.\n\n" +
                                "**Authentification**: OAuth2/JWT via Keycloak\n\n" +
                                "**Rôles**:\n" +
                                "- `USER`: Consultation et réservation de spectacles\n" +
                                "- `ADMIN`: Gestion complète des spectacles et accès aux statistiques")
                        .contact(new Contact()
                                .name("ARKENOP on GitHub"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Serveur de développement local"),
                        new Server().url("/").description("Serveur de production")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Authentification JWT via Keycloak. " +
                                                "Obtenez un token depuis Keycloak et incluez-le dans l'en-tête Authorization : Bearer {token}")));
    }
}

