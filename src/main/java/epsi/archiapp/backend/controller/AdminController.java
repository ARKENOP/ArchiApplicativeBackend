package epsi.archiapp.backend.controller;

import epsi.archiapp.backend.dto.StatsResponse;
import epsi.archiapp.backend.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
@CrossOrigin
public class AdminController {

    private final ReservationService reservationService;

    @GetMapping
    public StatsResponse getStatistics() {
        return reservationService.getStatistics();
    }
}

