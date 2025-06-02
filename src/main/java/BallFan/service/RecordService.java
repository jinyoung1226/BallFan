package BallFan.service;

import BallFan.authentication.UserDetailsServiceImpl;
import BallFan.dto.record.MyWinRateDTO;
import BallFan.dto.record.UserRankingDTO;
import BallFan.entity.Ticket;
import BallFan.entity.user.User;
import BallFan.exception.ticket.TicketNotFoundException;
import BallFan.repository.TicketRepository;
import BallFan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecordService {

    private final UserDetailsServiceImpl userDetailsService;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    /**
     * 나의 ?승 ?무 ?패 구하는 메서드
     * @return MyWinRateDTO
     */
    public MyWinRateDTO getMyWinRate() {
        User user = userDetailsService.getUserByContextHolder();
        return calculateMyWinRate(user);

    }

    /**
     * 내가 응원하는 팀 유저에서 승률 순위 나타내는 메서드
     * @return List<UserRankingDTO>
     */
    public List<UserRankingDTO> getTeamWinRateRanking() {
        User user = userDetailsService.getUserByContextHolder();
        List<User> teamUsers = userRepository.findByTeam(user.getTeam());

        // 유저별 승/무/패 집계 및 정렬
        List<UserRankingDTO> rankedList = teamUsers.stream()
                .map(user1 -> {
                    List<Ticket> tickets = ticketRepository.findByUserIdAndFavoriteTeam(user1.getId(), user1.getTeam());

                    int wins = 0;
                    int draws = 0;
                    int losses = 0;

                    for (Ticket ticket : tickets) {
                        switch (ticket.getIsWin()) {
                            case "승" -> wins++;
                            case "무" -> draws++;
                            case "패" -> losses++;
                        }
                    }

                    return new UserRankingDTO(
                            user1.getId(),
                            user1.getNickname(),
                            user1.getImage(),
                            wins,
                            losses,
                            draws,
                            0 // 초기 rank
                    );
                })
                .sorted((a, b) -> Integer.compare(b.getWinCount(), a.getWinCount())) // 승 수 내림차순
                .collect(Collectors.toList());

        // 순위 매기기
        int rank = 1;
        for (UserRankingDTO dto : rankedList) {
            dto.setRank(rank++);
        }

        return rankedList;
    }

    /**
     * 앱 내 유저 중 승률 순위 조회하는 메서드
     * @return List<UserRankingDTO>
     */
    public List<UserRankingDTO> getAppWinRateRanking() {
        User user = userDetailsService.getUserByContextHolder();
        List<User> allUser = userRepository.findAll();

        List<UserRankingDTO> rankedList = allUser.stream()
                .map(user1-> {
                    List<Ticket> tickets = ticketRepository.findByUserIdAndFavoriteTeam(user1.getId(), user1.getTeam());

                    int wins = 0;
                    int draws = 0;
                    int losses = 0;

                    for (Ticket ticket : tickets) {
                        switch (ticket.getIsWin()) {
                            case "승" -> wins++;
                            case "무" -> draws++;
                            case "패" -> losses++;
                        }
                    }

                    return new UserRankingDTO(
                            user1.getId(),
                            user1.getNickname(),
                            user1.getImage(),
                            wins,
                            losses,
                            draws,
                            0 // 초기 rank
                    );
                })
                .sorted((a, b) -> Integer.compare(b.getWinCount(), a.getWinCount())) // 승 수 기준 내림차순
                .collect(Collectors.toList());

        // 순위 부여
        int rank = 1;
        for (UserRankingDTO dto : rankedList) {
            dto.setRank(rank++);
        }

        return rankedList;
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

        int total = winCount + drawCount + loseCount;
        int winRate = total > 0 ? (int) Math.round((winCount * 100.0) / total) : 0;
        return new MyWinRateDTO(winCount, drawCount, loseCount, winRate);
    }


    public UserRankingDTO getVictoryFairy() {
        User user = userDetailsService.getUserByContextHolder();
        List<User> allUsers = userRepository.findAll();

        UserRankingDTO topUser = null;
        int maxWins = -1;

        for (User user1 : allUsers) {
            List<Ticket> tickets = ticketRepository.findByUserIdAndFavoriteTeam(user1.getId(), user1.getTeam());

            int winCount = 0;
            int drawCount = 0;
            int lossCount = 0;

            for (Ticket ticket : tickets) {
                switch (ticket.getIsWin()) {
                    case "승" -> winCount++;
                    case "무" -> drawCount++;
                    case "패" -> lossCount++;
                }
            }

            if (winCount > maxWins) {
                maxWins = winCount;
                topUser = new UserRankingDTO(
                        user1.getId(),
                        user1.getNickname(),
                        user1.getImage(),
                        winCount,
                        lossCount,
                        drawCount,
                        1 // 승리 요정은 1등
                );
            }
        }

        if (topUser == null) {
            throw new TicketNotFoundException("승리 요정을 찾을 수 없습니다.");
        }

        return topUser;
    }
}
