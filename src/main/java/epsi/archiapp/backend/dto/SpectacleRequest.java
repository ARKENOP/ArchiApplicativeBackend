package epsi.archiapp.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpectacleRequest {

    @NotBlank(message = "Le titre du spectacle est requis")
    @Size(min = 3, max = 200, message = "Le titre doit contenir entre 3 et 200 caractères")
    private String title;

    @Size(max = 2000, message = "La description ne peut pas dépasser 2000 caractères")
    private String description;

    @NotNull(message = "La date du spectacle est requise")
    @Future(message = "La date du spectacle doit être dans le futur")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime date;

    @NotNull(message = "Le prix du spectacle est requis")
    @DecimalMin(value = "0.01", message = "Le prix doit être supérieur à 0")
    @DecimalMax(value = "10000.00", message = "Le prix ne peut pas dépasser 10000€")
    private BigDecimal price;

    @NotNull(message = "Le nombre de billets disponibles est requis")
    @Min(value = 0, message = "Le nombre de billets ne peut pas être négatif")
    @Max(value = 10000, message = "Le nombre de billets ne peut pas dépasser 10000")
    private Integer availableTickets;

    @Size(max = 500, message = "L'URL de l'image ne peut pas dépasser 500 caractères")
    private String imageUrl;
}

