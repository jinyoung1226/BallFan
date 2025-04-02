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
public class OcrTicketDTO {

    private Team awayTeam;
    private String seat;
    private LocalDate ticketDate;

}
