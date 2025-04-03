package BallFan.service.ticket;

import BallFan.authentication.UserDetailsServiceImpl;
import BallFan.dto.ticket.OcrTicketDTO;
import BallFan.entity.GameResult;
import BallFan.entity.StadiumVisit;
import BallFan.entity.Team;
import BallFan.entity.Ticket;
import BallFan.entity.user.User;
import BallFan.repository.GameResultRepository;
import BallFan.repository.StadiumVisitRepository;
import BallFan.repository.TicketRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TicketService {

    private static final String GAME_RESULT_NOT_FOUND = "경기 결과를 찾을 수 없습니다";
    private final UserDetailsServiceImpl userDetailsService;
    private final WebClient webClient;
    private final GameResultRepository gameResultRepository;
    private final TicketRepository ticketRepository;
    private final ObjectMapper objectMapper;
    private final StadiumVisitRepository stadiumVisitRepository;

    /**
     * 종이 티켓 이미지를 받아, 종이티켓 OCR 서버로 넘겨주는 메서드
     * @param file
     */
    public void registerPaperTicket(MultipartFile file) {
        User user = userDetailsService.getUserByContextHolder();

        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();

            builder.part("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return "dummy.png"; // 꼭 넣어야 함
                }
            }).contentType(MediaType.APPLICATION_OCTET_STREAM);

            // 비동기 -> 동기 방식으로 전환하여 response에 Json 담기
            String response = webClient.post()
                    .uri("/upload_paperTicket")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Json -> 객체로 역직렬화
            // annotation JsonProperty 설정해야 같은 이름으로 판정되어 인식함
            OcrTicketDTO ocrTicketDTO = objectMapper.readValue(response, OcrTicketDTO.class);

            // OCR로 받은 어웨이팀과 날짜를 기반으로 경기 결과 DB를 조회하여 알맞는 경기 정보를 불러
            GameResult gameResult = gameResultRepository
                    .findByAwayTeamAndGameDate(ocrTicketDTO.getAwayTeam(), ocrTicketDTO.getTicketDate())
                    .orElseThrow(() -> new IllegalArgumentException(GAME_RESULT_NOT_FOUND));

            // 티켓을 등록할 때, 경기결과 스코어가 등록 되어있다면 내 팀을 기준으로 이겼는지 졌는지 true, false 넣어주기
            // 없다면 null 값으로 넣어주고, 무승부도 null
            Boolean isWin = null;
            if (gameResult.getScoreAwayTeam() != null && gameResult.getScoreHomeTeam() != null) {
                Team winnerTeam = null;

                if (gameResult.getScoreHomeTeam() > gameResult.getScoreAwayTeam()) {
                    winnerTeam = gameResult.getHomeTeam();
                } else if (gameResult.getScoreHomeTeam() < gameResult.getScoreAwayTeam()) {
                    winnerTeam = gameResult.getAwayTeam();
                }

                // 유저 팀이 경기 참여팀인지 확인
                boolean isUserTeamInvolved =
                        user.getTeam().equals(gameResult.getHomeTeam()) || user.getTeam().equals(gameResult.getAwayTeam());

                if (winnerTeam != null && isUserTeamInvolved) {
                    isWin = winnerTeam.equals(user.getTeam());
                } else {
                    // 유저 팀이 관련 없는 경기거나 무승부이면 isWin == null 유지
                    isWin = null;
                }
            }

            // Ticket 저장
            Ticket ticket = buildTicket(gameResult, ocrTicketDTO, isWin, user);
            ticketRepository.save(ticket);

            // 경기장 방문 테이블 저장
            StadiumVisit stadiumVisit = stadiumVisitRepository.findByUserIdAndStadium(user.getId(), gameResult.getStadium())
                    .orElseGet(() -> stadiumVisitRepository.save(
                            StadiumVisit.builder()
                                    .user(user)
                                    .visitCount(1)
                                    .stadium(gameResult.getStadium())
                                    .build()
                    ));

            // 이미 경기장 방문 기록이 있다면, 방문 횟수만 증가
            stadiumVisit.increaseVisitCount();


        } catch (IOException e) {
            throw new RuntimeException("이미지 전송 실패", e);
        }
    }

    public void registerPhoneTicket(MultipartFile file) {
        User user = userDetailsService.getUserByContextHolder();

        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();

            builder.part("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return "dummy.png"; // 꼭 넣어야 함
                }
            }).contentType(MediaType.APPLICATION_OCTET_STREAM);

            // 비동기 -> 동기 방식으로 전환하여 response에 Json 담기
            String response = webClient.post()
                    .uri("/upload_phoneTicket")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Json -> 객체로 역직렬화
            OcrTicketDTO ocrTicketDTO = objectMapper.readValue(response, OcrTicketDTO.class);

            // OCR로 받은 어웨이팀과 날짜를 기반으로 경기 결과 DB를 조회하여 알맞는 경기 정보를 불러
            GameResult gameResult = gameResultRepository
                    .findByAwayTeamAndGameDate(ocrTicketDTO.getAwayTeam(), ocrTicketDTO.getTicketDate())
                    .orElseThrow(() -> new IllegalArgumentException(GAME_RESULT_NOT_FOUND));

            // 티켓을 등록할 때, 경기결과 스코어가 등록 되어있다면 내 팀을 기준으로 이겼는지 졌는지 true, false 넣어주기
            // 없다면 null 값으로 넣어주고, 무승부도 null
            // 만약 내 팀이 아닌 다른 경기를 봤을 때도 생각해야 함....(고민)
            Boolean isWin = null;
            if(gameResult.getScoreAwayTeam() != null && gameResult.getScoreHomeTeam() != null) {
                Team winnerTeam = null;

                if (gameResult.getScoreHomeTeam() > gameResult.getScoreAwayTeam()) {
                    winnerTeam = gameResult.getHomeTeam();
                } else if (gameResult.getScoreHomeTeam() < gameResult.getScoreAwayTeam()) {
                    winnerTeam = gameResult.getAwayTeam();
                }

                // 유저 팀이 경기 참여팀인지 확인
                boolean isUserTeamInvolved =
                        user.getTeam().equals(gameResult.getHomeTeam()) || user.getTeam().equals(gameResult.getAwayTeam());

                if (winnerTeam != null && isUserTeamInvolved) {
                    isWin = winnerTeam.equals(user.getTeam());
                } else {
                    // 유저 팀이 관련 없는 경기거나 무승부이면 isWin == null 유지
                    isWin = null;
                }
            }

            // Ticket 저장
            Ticket ticket = buildTicket(gameResult, ocrTicketDTO, isWin, user);
            ticketRepository.save(ticket);

            // 경기장 방문 테이블 저장
            StadiumVisit stadiumVisit = stadiumVisitRepository.findByUserIdAndStadium(user.getId(), gameResult.getStadium())
                    .orElseGet(() -> stadiumVisitRepository.save(
                            StadiumVisit.builder()
                                    .user(user)
                                    .visitCount(1)
                                    .stadium(gameResult.getStadium())
                                    .build()
                    ));

            // 이미 경기장 방문 기록이 있다면, 방문 횟수만 증가
            stadiumVisit.increaseVisitCount();

        } catch (IOException e) {
            throw new RuntimeException("이미지 전송 실패", e);
        }
    }

    private Ticket buildTicket(GameResult gameResult, OcrTicketDTO ocrTicketDTO, Boolean isWin, User user) {
        return Ticket.builder()
                .homeTeam(gameResult.getHomeTeam())
                .awayTeam(gameResult.getAwayTeam())
                .ticketDate(ocrTicketDTO.getTicketDate())
                .seat(ocrTicketDTO.getSeat())
                .hasReview(false)
                .isWin(isWin)
                .createdDate(LocalDate.now())
                .gameResult(gameResult)
                .user(user)
                .build();
    }
}
