package epsi.archiapp.backend.mapper;

import epsi.archiapp.backend.dto.ReservationRequest;
import epsi.archiapp.backend.dto.ReservationResponse;
import epsi.archiapp.backend.model.Reservation;
import epsi.archiapp.backend.model.Spectacle;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ReservationMapper {

    public ReservationResponse toResponse(Reservation reservation) {
        if (reservation == null) {
            return null;
        }

        Spectacle spectacle = reservation.getSpectacle();

        return ReservationResponse.builder()
                .id(reservation.getId())
                .reservationDate(reservation.getReservationDate())
                .quantity(reservation.getQuantity())
                .totalPrice(reservation.getTotalPrice())
                .spectacle(ReservationResponse.SpectacleInfo.builder()
                        .id(spectacle.getId())
                        .title(spectacle.getTitle())
                        .date(spectacle.getDate())
                        .price(spectacle.getPrice())
                        .imageUrl(spectacle.getImageUrl())
                        .build())
                .build();
    }

    public Reservation toEntity(ReservationRequest request, Spectacle spectacle, String keycloakUserId, BigDecimal totalPrice) {
        if (request == null || spectacle == null) {
            return null;
        }

        return Reservation.builder()
                .keycloakUserId(keycloakUserId)
                .spectacle(spectacle)
                .quantity(request.getQuantity())
                .totalPrice(totalPrice)
                .build();
    }
}

