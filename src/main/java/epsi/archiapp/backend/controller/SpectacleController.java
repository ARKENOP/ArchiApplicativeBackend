package epsi.archiapp.backend.controller;

import epsi.archiapp.backend.model.Spectacle;
import epsi.archiapp.backend.service.SpectacleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/spectacles")
@RequiredArgsConstructor
@CrossOrigin
public class SpectacleController {
    private final SpectacleService spectacleService;

    @GetMapping
    public List<Spectacle> list() {
        return spectacleService.listAll();
    }

    @GetMapping("/{id}")
    public Spectacle get(@PathVariable Long id) {
        return spectacleService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Spectacle create(@Valid @RequestBody Spectacle spectacle) {
        return spectacleService.create(spectacle);
    }

    @PutMapping("/{id}")
    public Spectacle update(@PathVariable Long id, @Valid @RequestBody Spectacle spectacle) {
        return spectacleService.update(id, spectacle);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        spectacleService.delete(id);
    }
}
