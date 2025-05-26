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

        // 유저별 승패 집계 및 정렬
        List<UserRankingDTO> rankedList = teamUsers.stream()
                .map(user1 -> {
                    List<Ticket> tickets = ticketRepository.findByUserIdAndFavoriteTeam(user1.getId(), user1.getTeam());

                    int wins = 0;
                    int losses = 0;

                    for (Ticket ticket : tickets) {
                        if ("승".equals(ticket.getIsWin())) wins++;
                        else if ("패".equals(ticket.getIsWin())) losses++;
                    }

                    return new UserRankingDTO(
                            user1.getId(),
                            user1.getNickname(),
                            user1.getImage(),
                            wins,
                            losses,
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
                    int losses = 0;

                    for (Ticket ticket : tickets) {
                        if ("승".equals(ticket.getIsWin())) {
                            wins++;
                        } else if ("패".equals(ticket.getIsWin())) {
                            losses++;
                        }
                    }

                    return new UserRankingDTO(
                            user1.getId(),
                            user1.getNickname(),
                            user1.getImage(),
                            wins,
                            losses,
                            0 // 초기 순위
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

//    private List<UserRankingDTO> calculateRanking(List<User> userList, User user, int rankRange) {
//        // 1. 연승 기록이 있는 유저만 필터링 (null 제외, 0도 포함)
//        List<User> filteredUsers = new ArrayList<>();
//        for (User userOne : userList) {
//            if(userOne.getCurrentWinStreak() != null){
//                filteredUsers.add(userOne);
//            }
//        }
//
//        // 2. 연승(양수), 무승부(0), 연패(음수) 포함해서 내림차순 정렬
//        filteredUsers.sort(new Comparator<User>() {
//            @Override
//            public int compare(User u1, User u2) {
//                return u2.getCurrentWinStreak() - u1.getCurrentWinStreak();
//            }
//        });
//
//        // 3. 순위 계산 및 TOP 3 + 내 순위 추출
//        List<UserRankingDTO> result = new ArrayList<>();
//        int rank = 1;
//        UserRankingDTO myRanking = null;
//
//        for (User filteredUser : filteredUsers) {
//            UserRankingDTO userRankingDTO = new UserRankingDTO(
//                    filteredUser.getId(),
//                    filteredUser.getNickname(),
//                    filteredUser.getCurrentWinStreak(),
//                    rank);
//
//            if (user.getId().equals(filteredUser.getId())) {
//                myRanking = userRankingDTO;
//            }
//
//            if (rank <= rankRange) {
//                result.add(userRankingDTO);
//            }
//
//            rank++;
//        }
//
//        // 4. 본인이 TOP 3 안에 없으면 내 순위 추가
//        boolean alreadyIncluded = false;
//        for (UserRankingDTO dto : result) {
//            if (myRanking != null && dto.getId().equals(myRanking.getId())) {
//                alreadyIncluded = true;
//                break;
//            }
//        }
//
//        if (!alreadyIncluded && myRanking != null) {
//            result.add(myRanking);
//        }
//
//        return result;
//    }
}
