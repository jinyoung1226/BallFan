package BallFan.dto.ticket;

import BallFan.dto.line_up.LineUpDTO;
import BallFan.dto.pitcher.PitcherDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DetailTicketDTO {

    private String stadium;
    private LocalDate gameDate;
    private String dayOfWeek;
    private String homeTeam;
    private String awayTeam;
    private Integer scoreHomeTeam;
    private Integer scoreAwayTeam;
    private List<PitcherDTO> pitchers;
    private List<LineUpDTO> lineUps;
    private String seat;
    private Boolean isWin;
    private Boolean hasReview;
}
