package BallFan.dto.ticket;

import BallFan.entity.Team;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("away_team")
    private Team awayTeam;
    @JsonProperty("ticket_date")
    private LocalDate ticketDate;
    private String seat;

}
