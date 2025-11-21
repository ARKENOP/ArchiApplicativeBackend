package epsi.archiapp.backend.service;

import epsi.archiapp.backend.dto.SpectacleRequest;
import epsi.archiapp.backend.dto.SpectacleResponse;
import epsi.archiapp.backend.exception.ResourceNotFoundException;
import epsi.archiapp.backend.mapper.SpectacleMapper;
import epsi.archiapp.backend.model.Spectacle;
import epsi.archiapp.backend.repository.SpectacleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SpectacleService {

    private final SpectacleRepository spectacleRepository;
    private final SpectacleMapper spectacleMapper;

    public Page<SpectacleResponse> listAll(Pageable pageable) {
        log.debug("Récupération de la liste des spectacles - page: {}", pageable.getPageNumber());
        return spectacleRepository.findAll(pageable)
                .map(spectacleMapper::toResponse);
    }

    public SpectacleResponse get(Long id) {
        log.debug("Récupération du spectacle avec ID: {}", id);
        Spectacle spectacle = spectacleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Spectacle", "id", id));
        return spectacleMapper.toResponse(spectacle);
    }

    @Transactional
    public SpectacleResponse create(SpectacleRequest request) {
        log.info("Création d'un nouveau spectacle: {}", request.getTitle());
        Spectacle spectacle = spectacleMapper.toEntity(request);
        Spectacle saved = spectacleRepository.save(spectacle);
        log.info("Spectacle créé avec succès - ID: {}", saved.getId());
        return spectacleMapper.toResponse(saved);
    }

    @Transactional
    public SpectacleResponse update(Long id, SpectacleRequest request) {
        log.info("Mise à jour du spectacle avec ID: {}", id);
        Spectacle spectacle = spectacleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Spectacle", "id", id));

        spectacleMapper.updateEntityFromRequest(request, spectacle);
        Spectacle updated = spectacleRepository.save(spectacle);
        log.info("Spectacle mis à jour avec succès - ID: {}", updated.getId());
        return spectacleMapper.toResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Suppression du spectacle avec ID: {}", id);
        if (!spectacleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Spectacle", "id", id);
        }
        spectacleRepository.deleteById(id);
        log.info("Spectacle supprimé avec succès - ID: {}", id);
    }
}

