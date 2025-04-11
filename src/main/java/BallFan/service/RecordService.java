package BallFan.service;

import BallFan.authentication.UserDetailsServiceImpl;
import BallFan.dto.record.MyWinRateDTO;
import BallFan.dto.record.UserRankingDTO;
import BallFan.entity.Ticket;
import BallFan.entity.user.User;
import BallFan.repository.TicketRepository;
import BallFan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
     * 내가 응원하는 팀 유저 중 연승 기록 구하는 메서드
     * @return List<UserRankingDTO>
     */
    public List<UserRankingDTO> getTeamWinRateRanking() {
        User user = userDetailsService.getUserByContextHolder();

        List<User> myTeamUsers = userRepository.findByTeam(user.getTeam());

        return calculateRanking(myTeamUsers, user, 3);
    }

    /**
     * 앱 내 유저 중 연승 기록 구하는 메서드
     * @return List<UserRankingDTO>
     */
    public List<UserRankingDTO> getAppWinRateRanking() {
        User user = userDetailsService.getUserByContextHolder();

        List<User> allUser = userRepository.findAll();

        return calculateRanking(allUser, user, 10);
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

    private List<UserRankingDTO> calculateRanking(List<User> userList, User user, int rankRange) {
        // 1. 연승 기록이 있는 유저만 필터링 (null 제외, 0도 포함)
        List<User> filteredUsers = new ArrayList<>();
        for (User userOne : userList) {
            if(userOne.getCurrentWinStreak() != null){
                filteredUsers.add(userOne);
            }
        }

        // 2. 연승(양수), 무승부(0), 연패(음수) 포함해서 내림차순 정렬
        filteredUsers.sort(new Comparator<User>() {
            @Override
            public int compare(User u1, User u2) {
                return u2.getCurrentWinStreak() - u1.getCurrentWinStreak();
            }
        });

        // 3. 순위 계산 및 TOP 3 + 내 순위 추출
        List<UserRankingDTO> result = new ArrayList<>();
        int rank = 1;
        UserRankingDTO myRanking = null;

        for (User filteredUser : filteredUsers) {
            UserRankingDTO userRankingDTO = new UserRankingDTO(
                    filteredUser.getId(),
                    filteredUser.getNickname(),
                    filteredUser.getCurrentWinStreak(),
                    rank);

            if (user.getId().equals(filteredUser.getId())) {
                myRanking = userRankingDTO;
            }

            if (rank <= rankRange) {
                result.add(userRankingDTO);
            }

            rank++;
        }

        // 4. 본인이 TOP 3 안에 없으면 내 순위 추가
        boolean alreadyIncluded = false;
        for (UserRankingDTO dto : result) {
            if (myRanking != null && dto.getId().equals(myRanking.getId())) {
                alreadyIncluded = true;
                break;
            }
        }

        if (!alreadyIncluded && myRanking != null) {
            result.add(myRanking);
        }

        return result;
    }
}
