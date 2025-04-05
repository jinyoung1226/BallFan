package BallFan.controller;

import BallFan.dto.ticket.DetailTicketDTO;
import BallFan.dto.ticket.TicketPreviewDTO;
import BallFan.service.ticket.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/ticket")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping
    public ResponseEntity<List<TicketPreviewDTO>> getTicket() {
        List<TicketPreviewDTO> tickets = ticketService.getTicket();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetailTicketDTO> getTicketDetail(@PathVariable Long id) {
        DetailTicketDTO ticketDetail = ticketService.getTicketDetail(id);
        return ResponseEntity.ok(ticketDetail);
    }

    @PostMapping("/paper")
    public ResponseEntity<Void> registerPaperTicket(@RequestPart MultipartFile file) {
        ticketService.registerPaperTicket(file);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/phone")
    public ResponseEntity<Void> registerPhoneTicket(@RequestPart MultipartFile file) {
        ticketService.registerPhoneTicket(file);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/image/{id}")
    public ResponseEntity<Void> registerTicketImage(@PathVariable Long id, @RequestPart MultipartFile file) {
        ticketService.registerTicketImage(id, file);
        return ResponseEntity.ok().build();
    }

}
