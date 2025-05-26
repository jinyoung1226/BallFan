package BallFan.controller;

import BallFan.dto.record.MyWinRateDTO;
import BallFan.dto.record.UserRankingDTO;
import BallFan.service.RecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/record")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;

    @GetMapping("/my-win-rate")
    public ResponseEntity<MyWinRateDTO> getMyWinRate() {
        MyWinRateDTO myWinRate = recordService.getMyWinRate();
        return ResponseEntity.ok(myWinRate);
    }

    @GetMapping("/team-win-rate-ranking")
    public ResponseEntity<List<UserRankingDTO>> getTeamWinRateRanking() {
        List<UserRankingDTO> teamWinRateRanking = recordService.getTeamWinRateRanking();
        return ResponseEntity.ok(teamWinRateRanking);
    }

    @GetMapping("/app-win-rate-ranking")
    public ResponseEntity<List<UserRankingDTO>> getAppWinRateRanking() {
        List<UserRankingDTO> appWinRateRanking = recordService.getAppWinRateRanking();
        return ResponseEntity.ok(appWinRateRanking);
    }

    @GetMapping("/victory-fairy")
    public ResponseEntity<UserRankingDTO> getVictoryFairy() {
        UserRankingDTO victoryFairy = recordService.getVictoryFairy();
        return ResponseEntity.ok(victoryFairy);
    }



}
