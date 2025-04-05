package BallFan.service.ticket;

import BallFan.authentication.UserDetailsServiceImpl;
import BallFan.dto.line_up.LineUpDTO;
import BallFan.dto.pitcher.PitcherDTO;
import BallFan.dto.ticket.DetailTicketDTO;
import BallFan.dto.ticket.OcrTicketDTO;
import BallFan.dto.ticket.TicketPreviewDTO;
import BallFan.entity.*;
import BallFan.entity.pitcher.PitcherStat;
import BallFan.entity.user.User;
import BallFan.exception.ticket.DuplicatedTicketException;
import BallFan.exception.ticket.TicketNotFoundException;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class TicketService {

    private static final String GAME_RESULT_NOT_FOUND_MESSAGE = "경기 결과를 찾을 수 없습니다";
    private static final String DUPLICATED_TICKET_MESSAGE = "이미 등록된 티켓입니다";
    private static final String TICKET_NOT_FOUND_MESSAGE = "티켓이 존재하지 않습니다";
    private final UserDetailsServiceImpl userDetailsService;
    private final WebClient webClient;
    private final GameResultRepository gameResultRepository;
    private final TicketRepository ticketRepository;
    private final ObjectMapper objectMapper;
    private final StadiumVisitRepository stadiumVisitRepository;

    /**
     * 티켓 조회하는 메서드
     * @return List<TicketPreviewDTO>
     */
    public List<TicketPreviewDTO> getTicket() {
        User user = userDetailsService.getUserByContextHolder();

        List<Ticket> tickets = ticketRepository.findByUserId(user.getId());
        if (tickets.isEmpty()) {
            throw new TicketNotFoundException(TICKET_NOT_FOUND_MESSAGE);
        }

        List<TicketPreviewDTO> ticketPreviewDTOs = buildTicketPreviewDTO(tickets);
        return ticketPreviewDTOs;
    }

    /**
     * 티켓 상세정보 조회하는 메서드
     * @param ticketId
     * @return
     */
    public DetailTicketDTO getTicketDetail(Long ticketId) {
        User user = userDetailsService.getUserByContextHolder();

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(TICKET_NOT_FOUND_MESSAGE));

        return buildDetailTicketDTO(ticket);
    }

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

    /**
     * 스마트 티켓 이미지를 받아, 스마트 티켓 OCR 서버로 넘겨주는 메서드
     * @param file
     */
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

    private DetailTicketDTO buildDetailTicketDTO(Ticket ticket) {
        List<PitcherDTO> pitcherDTOs = mapToPitcherDTOs(ticket);
        List<LineUpDTO> lineUpDTOs = mapToLineUpDTOs(ticket);

        DayOfWeek dayOfWeek = ticket.getGameResult().getGameDate().getDayOfWeek();
        String koreanDay = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN);

        return DetailTicketDTO.builder()
                .stadium(ticket.getGameResult().getStadium())
                .gameDate(ticket.getGameResult().getGameDate())
                .dayOfWeek(koreanDay)
                .homeTeam(ticket.getGameResult().getHomeTeam().toString())
                .awayTeam(ticket.getGameResult().getAwayTeam().toString())
                .scoreHomeTeam(ticket.getGameResult().getScoreHomeTeam())
                .scoreAwayTeam(ticket.getGameResult().getScoreAwayTeam())
                .pitchers(pitcherDTOs)
                .lineUps(lineUpDTOs)
                .seat(ticket.getSeat())
                .isWin(ticket.getIsWin())
                .hasReview(ticket.isHasReview())
                .build();
    }

    private List<PitcherDTO> mapToPitcherDTOs(Ticket ticket) {
        List<PitcherDTO> pitcherDTOs = new ArrayList<>();
        for (PitcherStat pitcher : ticket.getGameResult().getPitcherStats()) {
            pitcherDTOs.add(new PitcherDTO(pitcher.getName(), pitcher.getTeam(), pitcher.getPitcherType()));
        }
        return pitcherDTOs;
    }

    private List<LineUpDTO> mapToLineUpDTOs(Ticket ticket) {
        List<LineUpDTO> lineUpDTOs = new ArrayList<>();
        for (LineUp lineUp : ticket.getGameResult().getLineUps()) {
            lineUpDTOs.add(new LineUpDTO(lineUp.getName(), lineUp.getOrder(), lineUp.getPosition(), lineUp.getTeam()));
        }
        return lineUpDTOs;
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

    private List<TicketPreviewDTO> buildTicketPreviewDTO(List<Ticket> tickets) {
        List<TicketPreviewDTO> results = new ArrayList<>();

        for (Ticket ticket : tickets) {
            DayOfWeek dayOfWeek = ticket.getTicketDate().getDayOfWeek();
            String koreanDay = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN);

            TicketPreviewDTO ticketPreviewDTO = TicketPreviewDTO.builder()
                    .id(ticket.getId())
                    .stadium(ticket.getGameResult().getStadium())
                    .ticketDate(ticket.getTicketDate())
                    .dayOfWeek(koreanDay)
                    .homeTeam(ticket.getHomeTeam().toString())
                    .awayTeam(ticket.getAwayTeam().toString())
                    .build();

            results.add(ticketPreviewDTO);
        }

        return results;
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
                .orElseThrow(() -> new IllegalArgumentException(GAME_RESULT_NOT_FOUND_MESSAGE));
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
