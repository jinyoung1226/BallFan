package BallFan.controller;

import BallFan.dto.record.MyWinRateDTO;
import BallFan.service.RecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/record")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;

    @GetMapping("/my-rate")
    public ResponseEntity<MyWinRateDTO> getMyWinRate() {
        MyWinRateDTO myWinRate = recordService.getMyWinRate();
        return ResponseEntity.ok(myWinRate);
    }
}
