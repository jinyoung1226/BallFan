package BallFan.dto.ticket;

import BallFan.entity.Team;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketPreviewDTO {

    private Long id;
    private String stadium;
    private LocalDate ticketDate;
    private String dayOfWeek;
    private String homeTeam;
    private String awayTeam;

}
