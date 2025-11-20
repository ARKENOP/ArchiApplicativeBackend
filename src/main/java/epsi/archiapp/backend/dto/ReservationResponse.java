package epsi.archiapp.backend.dto;

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
public class ReservationResponse {
    private Long id;
    private LocalDateTime reservationDate;
    private Integer quantity;
    private BigDecimal totalPrice;
    private SpectacleInfo spectacle;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SpectacleInfo {
        private Long id;
        private String title;
        private LocalDateTime date;
        private BigDecimal price;
        private String imageUrl;
    }
}

