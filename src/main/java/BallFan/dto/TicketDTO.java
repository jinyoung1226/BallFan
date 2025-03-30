package BallFan.dto;

import BallFan.entity.GameResult;
import BallFan.entity.Team;
import BallFan.entity.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketDTO {

    private int id;
    private Team homeTeam;
    private Team awayTeam;
    private LocalDate ticketDate;
    private String seat;
    private LocalDate createdDate;
    private boolean isWin;
    private boolean hasReview;
    private User user;
    private GameResult gameResult;
}
