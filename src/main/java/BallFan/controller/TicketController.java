package BallFan.controller;

import BallFan.service.ticket.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/ticket")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/paper")
    public ResponseEntity<Void> registerPaperTicket(@RequestPart MultipartFile file) {
        ticketService.registerPaperTicket(file);
        return ResponseEntity.ok().build();
    }

}
