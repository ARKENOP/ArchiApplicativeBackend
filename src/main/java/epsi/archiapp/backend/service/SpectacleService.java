package epsi.archiapp.backend.service;

import epsi.archiapp.backend.model.Spectacle;
import epsi.archiapp.backend.repository.SpectacleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpectacleService {
    private final SpectacleRepository spectacleRepository;

    public List<Spectacle> listAll() {
        return spectacleRepository.findAll();
    }

    public Spectacle get(Long id) {
        return spectacleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Spectacle not found"));
    }

    public Spectacle create(Spectacle spectacle) {
        return spectacleRepository.save(spectacle);
    }

    public Spectacle update(Long id, Spectacle updated) {
        Spectacle s = get(id);
        s.setTitle(updated.getTitle());
        s.setDescription(updated.getDescription());
        s.setDate(updated.getDate());
        s.setPrice(updated.getPrice());
        s.setAvailableTickets(updated.getAvailableTickets());
        s.setImageUrl(updated.getImageUrl());
        return spectacleRepository.save(s);
    }

    public void delete(Long id) {
        spectacleRepository.deleteById(id);
    }
}

