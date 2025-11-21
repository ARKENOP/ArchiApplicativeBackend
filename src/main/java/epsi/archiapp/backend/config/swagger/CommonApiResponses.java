package epsi.archiapp.backend.config.swagger;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.*;

/**
 * Annotations composées pour les réponses API communes
 */
public class CommonApiResponses {

    /**
     * Réponses standards pour une opération de lecture (GET)
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Succès"),
        @ApiResponse(responseCode = "404", description = "Ressource non trouvée")
    })
    public @interface GetApiResponses {}

    /**
     * Réponses standards pour une liste paginée (GET)
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès"),
        @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    public @interface ListApiResponses {}

    /**
     * Réponses standards pour une création (POST)
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Créé avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public @interface CreateApiResponses {}

    /**
     * Réponses standards pour une mise à jour (PUT)
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Mis à jour avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "404", description = "Ressource non trouvée")
    })
    public @interface UpdateApiResponses {}

    /**
     * Réponses standards pour une suppression (DELETE)
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Supprimé avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "404", description = "Ressource non trouvée")
    })
    public @interface DeleteApiResponses {}

    /**
     * Réponses standards pour les opérations nécessitant le rôle ADMIN
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "403", description = "Accès interdit - rôle ADMIN requis")
    })
    public @interface AdminApiResponses {}

    /**
     * Réponses standards pour les opérations protégées par authentification
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès interdit")
    })
    public @interface AuthApiResponses {}
}

