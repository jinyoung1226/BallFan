package BallFan.controller;

import BallFan.dto.ticket.DetailTicketDTO;
import BallFan.dto.ticket.HomeResponseDTO;
import BallFan.dto.ticket.ScanTicketDTO;
import BallFan.entity.Team;
import BallFan.service.ticket.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@RestController
@RequestMapping("/ticket")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping
    public ResponseEntity<HomeResponseDTO> getTicket() {
        HomeResponseDTO homeResponseDTO = ticketService.getTicket();
        return ResponseEntity.ok(homeResponseDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetailTicketDTO> getTicketDetail(@PathVariable Long id) {
        DetailTicketDTO ticketDetail = ticketService.getTicketDetail(id);
        return ResponseEntity.ok(ticketDetail);
    }

    @PostMapping("/paper/scan")
    public ResponseEntity<ScanTicketDTO> scanPaperTicket(@RequestPart MultipartFile file) {
        ScanTicketDTO scanTicketDTO = ticketService.scanPaperTicket(file);
        return ResponseEntity.ok(scanTicketDTO);
    }

    @PostMapping("/paper/register")
    public ResponseEntity<Void> registerPaperTicket(@RequestParam(name = "awayTeam") Team awayTeam,
                                                    @RequestParam(name = "gameDate")LocalDate gameDate,
                                                    @RequestParam(name = "seat") String seat) {
        ticketService.registerPaperTicket(awayTeam, gameDate, seat);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/image/{id}")
    public ResponseEntity<Void> registerTicketImage(@PathVariable Long id, @RequestPart MultipartFile file) {
        ticketService.registerTicketImage(id, file);
        return ResponseEntity.ok().build();
    }

}
