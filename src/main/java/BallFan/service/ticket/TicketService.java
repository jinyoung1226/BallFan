package BallFan.service.ticket;

import BallFan.authentication.UserDetailsServiceImpl;
import BallFan.dto.ticket.OcrTicketDTO;
import BallFan.entity.GameResult;
import BallFan.entity.StadiumVisit;
import BallFan.entity.Team;
import BallFan.entity.Ticket;
import BallFan.entity.user.User;
import BallFan.exception.ticket.DuplicatedTicketException;
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
    private static final String DUPLICATED_TICKET_MESSAGE = "이미 등록된 티켓입니다";
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

        // 종이티켓 OCR 등록
        OcrTicketDTO ocrTicketDTO = requestPaperTicketOcr(file);
        // 경기 결과 조회
        GameResult gameResult = findGameResult(ocrTicketDTO);
        // 경기 결과 중복 확인
        validateDuplicateTicket(user, gameResult, ocrTicketDTO);
        // 내가 응원하는 팀 승리 여부 판단
        Boolean isWin = determineIsWin(user, gameResult);
        // 티켓 저장
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
    }

    public void registerPhoneTicket(MultipartFile file) {
        User user = userDetailsService.getUserByContextHolder();

        // 스마트티켓 OCR 등록
        OcrTicketDTO ocrTicketDTO = requestPhoneTicketOcr(file);
        // 경기 결과 조회
        GameResult gameResult = findGameResult(ocrTicketDTO);
        // 경기 결과 중복 확인
        validateDuplicateTicket(user, gameResult, ocrTicketDTO);
        // 내가 응원하는 팀 승리 여부 판단
        Boolean isWin = determineIsWin(user, gameResult);
        // 티켓 저장
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

    private OcrTicketDTO requestPaperTicketOcr(MultipartFile file) {
        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();

            builder.part("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return "dummy.png";
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
            return objectMapper.readValue(response, OcrTicketDTO.class);

        } catch (IOException e) {
            throw new RuntimeException("이미지 전송 실패", e);
        }
    }

    private OcrTicketDTO requestPhoneTicketOcr(MultipartFile file) {
        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();

            builder.part("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return "dummy.png";
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
            // annotation JsonProperty 설정해야 같은 이름으로 판정되어 인식함
            return objectMapper.readValue(response, OcrTicketDTO.class);

        } catch (IOException e) {
            throw new RuntimeException("이미지 전송 실패", e);
        }
    }

    private GameResult findGameResult(OcrTicketDTO dto) {
        return gameResultRepository
                .findByAwayTeamAndGameDate(dto.getAwayTeam(), dto.getTicketDate())
                .orElseThrow(() -> new IllegalArgumentException(GAME_RESULT_NOT_FOUND));
    }

    private Boolean determineIsWin(User user, GameResult gameResult) {
        if (gameResult.getScoreAwayTeam() == null || gameResult.getScoreHomeTeam() == null) return null;

        Team winnerTeam = null;

        if (gameResult.getScoreHomeTeam() > gameResult.getScoreAwayTeam()) {
            winnerTeam = gameResult.getHomeTeam();
        } else if (gameResult.getScoreHomeTeam() < gameResult.getScoreAwayTeam()) {
            winnerTeam = gameResult.getAwayTeam();
        }

        boolean isUserTeamInvolved =
                user.getTeam().equals(gameResult.getHomeTeam()) || user.getTeam().equals(gameResult.getAwayTeam());

        return (winnerTeam != null && isUserTeamInvolved)
                ? winnerTeam.equals(user.getTeam())
                : null;
    }

    private void validateDuplicateTicket(User user, GameResult gameResult, OcrTicketDTO dto) {
        boolean exists = ticketRepository.findByUserIdAndTicketDateAndHomeTeamAndAwayTeam(
                user.getId(), dto.getTicketDate(), gameResult.getHomeTeam(), gameResult.getAwayTeam()
        ).isPresent();

        if (exists) {
            throw new DuplicatedTicketException(DUPLICATED_TICKET_MESSAGE);
        }
    }
}
