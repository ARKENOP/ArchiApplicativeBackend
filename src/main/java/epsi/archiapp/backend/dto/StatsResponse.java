package epsi.archiapp.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatsResponse {
    private BigDecimal totalRevenue;
    private Long totalReservations;
    private List<SpectacleSales> salesBySpectacle;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SpectacleSales {
        private Long spectacleId;
        private String title;
        private Long ticketsSold;
        private BigDecimal revenue;
    }
}

