package epsi.archiapp.backend.mapper;

import epsi.archiapp.backend.dto.SpectacleRequest;
import epsi.archiapp.backend.dto.SpectacleResponse;
import epsi.archiapp.backend.model.Spectacle;
import org.springframework.stereotype.Component;

@Component
public class SpectacleMapper {

    public SpectacleResponse toResponse(Spectacle spectacle) {
        if (spectacle == null) {
            return null;
        }

        return SpectacleResponse.builder()
                .id(spectacle.getId())
                .title(spectacle.getTitle())
                .description(spectacle.getDescription())
                .date(spectacle.getDate())
                .price(spectacle.getPrice())
                .availableTickets(spectacle.getAvailableTickets())
                .imageUrl(spectacle.getImageUrl())
                .createdAt(spectacle.getCreatedAt())
                .updatedAt(spectacle.getUpdatedAt())
                .createdBy(spectacle.getCreatedBy())
                .updatedBy(spectacle.getUpdatedBy())
                .build();
    }

    public Spectacle toEntity(SpectacleRequest request) {
        if (request == null) {
            return null;
        }

        return Spectacle.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .date(request.getDate())
                .price(request.getPrice())
                .availableTickets(request.getAvailableTickets())
                .imageUrl(request.getImageUrl())
                .build();
    }

    public void updateEntityFromRequest(SpectacleRequest request, Spectacle spectacle) {
        if (request == null || spectacle == null) {
            return;
        }

        spectacle.setTitle(request.getTitle());
        spectacle.setDescription(request.getDescription());
        spectacle.setDate(request.getDate());
        spectacle.setPrice(request.getPrice());
        spectacle.setAvailableTickets(request.getAvailableTickets());
        spectacle.setImageUrl(request.getImageUrl());
    }
}

