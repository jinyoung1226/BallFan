package BallFan.service;

import BallFan.authentication.UserDetailsServiceImpl;
import BallFan.dto.record.MyWinRateDTO;
import BallFan.entity.Team;
import BallFan.entity.Ticket;
import BallFan.entity.user.User;
import BallFan.repository.TicketRepository;
import BallFan.service.ticket.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecordService {

    private final UserDetailsServiceImpl userDetailsService;
    private final TicketRepository ticketRepository;

    public MyWinRateDTO getMyWinRate() {
        User user = userDetailsService.getUserByContextHolder();
        return calculateMyWinRate(user);

    }

    private MyWinRateDTO calculateMyWinRate(User user) {
        List<Ticket> findTickets = ticketRepository.findByUserIdAndFavoriteTeam(user.getId(), user.getTeam());
        if(findTickets.isEmpty()) {
            return new MyWinRateDTO();
        }

        int winCount = 0;
        int drawCount = 0;
        int loseCount = 0;

        for (Ticket ticket : findTickets) {
            if(ticket.getIsWin().equals("승")) {
                winCount++;
            } else if (ticket.getIsWin().equals("패")) {
                loseCount++;
            } else if (ticket.getIsWin().equals("무")) {
                drawCount++;
            }
        }
        return new MyWinRateDTO(winCount, drawCount, loseCount);
    }
}
