package BallFan.controller;

import BallFan.dto.stadium.StadiumVisitDTO;
import BallFan.service.StadiumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/stadium")
@RequiredArgsConstructor
public class StadiumVisitController {

    private final StadiumService stadiumService;

    @GetMapping
    public ResponseEntity<List<StadiumVisitDTO>> getStadiumVisits() {
        List<StadiumVisitDTO> stadiumVisits = stadiumService.getStadiumVisits();
        return ResponseEntity.ok(stadiumVisits);
    }
}
