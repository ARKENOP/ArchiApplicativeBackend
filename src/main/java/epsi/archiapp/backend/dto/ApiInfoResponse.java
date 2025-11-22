package epsi.archiapp.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Informations sur l'application")
public class ApiInfoResponse {

    @Schema(description = "Nom de l'application", example = "API Réservation Théâtre")
    private String name;

    @Schema(description = "Version de l'application", example = "1.0.0")
    private String version;

    @Schema(description = "Description de l'application")
    private String description;

    @Schema(description = "Environnement d'exécution", example = "dev")
    private String environment;
}

