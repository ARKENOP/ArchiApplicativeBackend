package epsi.archiapp.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.*;

@Entity
@Table(name = "spectacles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Spectacle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @NotNull
    private LocalDateTime date;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal price;

    @Column(name = "available_tickets", nullable = false)
    @NotNull
    @Min(0)
    private Integer availableTickets;

    @Column(name = "image_url")
    private String imageUrl;

    @Version
    private Long version;
}
