package epsi.archiapp.backend.repository;

import epsi.archiapp.backend.model.Spectacle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpectacleRepository extends JpaRepository<Spectacle, Long> {
    Spectacle findByTitle(String title);
}

