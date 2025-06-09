package BallFan.service.ticket;

import BallFan.authentication.UserDetailsServiceImpl;
import BallFan.dto.line_up.LineUpDTO;
import BallFan.dto.pitcher.PitcherDTO;
import BallFan.dto.ticket.*;
import BallFan.entity.*;
import BallFan.entity.pitcher.PitcherStat;
import BallFan.entity.user.User;
import BallFan.exception.ticket.DuplicatedTicketException;
import BallFan.exception.ticket.TicketDetailNotFoundException;
import BallFan.exception.ticket.TicketNotFoundException;
import BallFan.repository.GameResultRepository;
import BallFan.repository.StadiumVisitRepository;
import BallFan.repository.TicketRepository;
import BallFan.s3.S3Uploader;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TicketService {

    @Value("${cloud.aws.s3.bucket}")
    private String DirName;
    private static final String GAME_RESULT_NOT_FOUND_MESSAGE = "경기 결과를 찾을 수 없습니다";
    private static final String DUPLICATED_TICKET_MESSAGE = "이미 등록된 티켓입니다";
    private static final String TICKET_NOT_FOUND_MESSAGE = "티켓이 존재하지 않습니다";
    private static final String TICKET_DETAIL_NOT_FOUND_MESSAGE = "티켓이 상세정보가 존재하지 않습니다";
    private static final Map<String,String> POSITION_TRANSLATION = Map.ofEntries(
            Map.entry("투수", "P"),
            Map.entry("포수", "C"),
            Map.entry("1루수", "1B"),
            Map.entry("2루수", "2B"),
            Map.entry("3루수", "3B"),
            Map.entry("유격수", "SS"),
            Map.entry("좌익수", "LF"),
            Map.entry("중견수", "CF"),
            Map.entry("우익수", "RF"),
            Map.entry("지명타자", "DH")
    );
    private final UserDetailsServiceImpl userDetailsService;
    private final WebClient webClient;
    private final GameResultRepository gameResultRepository;
    private final TicketRepository ticketRepository;
    private final ObjectMapper objectMapper;
    private final StadiumVisitRepository stadiumVisitRepository;
    private final S3Uploader s3Uploader;

    /**
     * 티켓 조회하고 내부적으로 연승 계산하는 메서드
     * @return List<TicketPreviewDTO>
     */
    @Transactional
    public HomeResponseDTO getTicket() {
        User user = userDetailsService.getUserByContextHolder();

        List<Ticket> tickets = ticketRepository.findByUserId(user.getId());

        // 티켓이 없는 경우: 연승(null) + 빈 리스트 또는 비어 있는 DTO 리스트 반환
        if (tickets.isEmpty()) {
            return new HomeResponseDTO(null, buildTicketPreviewDTO(tickets));
        }

        // 여기부터 조회할 때마다 연승 계산을 최신화를 반영하여 계산하기 위한 로직
        // 1. isWin == null && 내가 응원하는 팀이 경기한 티켓만 필터링
        List<Ticket> needToEvaluate = tickets.stream()
                .filter(ticket -> ticket.getIsWin() == null)
                .filter(ticket ->
                        ticket.getGameResult().getHomeTeam().equals(user.getTeam()) ||
                                ticket.getGameResult().getAwayTeam().equals(user.getTeam()))
                .toList();

        // 2. 해당 티켓들에 대해 승/패/무 계산
        for (Ticket ticket : needToEvaluate) {
            String result = determineWinStatus(user, ticket.getGameResult());
            ticket.updateIsWin(result);
        }

        // 3. 연승 계산 및 DTO 변환
        Integer streak = calculateWinningStreak(user);
        List<TicketPreviewDTO> ticketPreviewDTOs = buildTicketPreviewDTO(tickets);

        // 4. 연승 기록 User entity에 저장
        user.updateCurrentWinStreak(streak);

        return new HomeResponseDTO(streak, ticketPreviewDTOs);
    }

    /**
     * 티켓 상세정보 조회하는 메서드
     * @param ticketId 조회할 티켓의 ID
     * @return 상세 티켓 정보 DTO
     */
    public DetailTicketDTO getTicketDetail(Long ticketId) {
        User user = userDetailsService.getUserByContextHolder();

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketDetailNotFoundException(TICKET_DETAIL_NOT_FOUND_MESSAGE));

        return buildDetailTicketDTO(ticket);
    }


    /**
     * 티켓 스캔해서 DTO 반환하는 메서드
     * @param file 등록할 티켓 파일 이미지
     */
    public ScanTicketDTO scanPaperTicket(MultipartFile file) {
        User user = userDetailsService.getUserByContextHolder();

        // 종이티켓 OCR
        OcrTicketDTO ocrTicketDTO = requestPaperTicketOcr(file);
        System.out.println(ocrTicketDTO.getTicketDate());
        System.out.println(ocrTicketDTO.getAwayTeam());
        System.out.println(ocrTicketDTO.getSeat());
        // 경기 결과 조회
        GameResult gameResult = findGameResult(ocrTicketDTO);
        // 경기 결과 중복 확인
        validateDuplicateTicket(user, gameResult, ocrTicketDTO);

        return ScanTicketDTO.builder()
                .gameDate(gameResult.getGameDate())
                .stadium(gameResult.getStadium())
                .homeTeam(gameResult.getHomeTeam())
                .awayTeam(gameResult.getAwayTeam())
                .seat(ocrTicketDTO.getSeat())
                .build();
    }


    /**
     * 티켓 등록하는 메서드
     * @param awayTeam
     * @param seat
     * @param gameDate
     */
    @Transactional
    public void registerPaperTicket(Team awayTeam, LocalDate gameDate, String seat) {
        User user = userDetailsService.getUserByContextHolder();

        GameResult gameResult = gameResultRepository.findByAwayTeamAndGameDate(awayTeam, gameDate)
                .orElseThrow(() -> new IllegalArgumentException(GAME_RESULT_NOT_FOUND_MESSAGE));

        // 내가 응원하는 팀 승리 여부 판단
        String isWin = determineWinStatus(user, gameResult);
        System.out.println(isWin);
        // 티켓 저장
        Ticket ticket = buildTicket(gameResult, gameDate, seat, isWin, user);
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
     * 티켓 이미지 등록하는 메서드
     * @param ticketId
     * @param imageFile
     */
    @Transactional
    public void registerTicketImage(Long ticketId, MultipartFile imageFile) {
        User user = userDetailsService.getUserByContextHolder();

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(TICKET_NOT_FOUND_MESSAGE));

        String imagePath = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imagePath = s3Uploader.uploadImage(imageFile, DirName);
            ticket.updateImage(imagePath);
        } else {
            user.updateImage(null);
        }
    }

    private DetailTicketDTO buildDetailTicketDTO(Ticket ticket) {
        List<PitcherDTO> pitcherDTOs = mapToPitcherDTOs(ticket);
        List<LineUpDTO> lineUpDTOs = mapToLineUpDTOs(ticket);

        // 하나라도 null이면 빈 DetailTicketDTO 반환
        if (pitcherDTOs.isEmpty() || lineUpDTOs.isEmpty()) {
            return DetailTicketDTO.builder()
                    .build();
        }

        DayOfWeek dayOfWeek = ticket.getGameResult().getGameDate().getDayOfWeek();
        String koreanDay = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN);

        boolean hasReview = ticket.getReview() != null;

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
                .hasReview(hasReview)
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
            String koreanPosition = lineUp.getPosition();
            String englishPosition = POSITION_TRANSLATION.getOrDefault(koreanPosition, koreanPosition); // 매핑 안 되어 있으면 원래 값

            lineUpDTOs.add(new LineUpDTO(lineUp.getName(), lineUp.getOrder(), englishPosition, lineUp.getTeam()));
        }
        return lineUpDTOs;
    }

    private Ticket buildTicket(GameResult gameResult, LocalDate gameDate, String seat, String isWin, User user) {
        return Ticket.builder()
                .homeTeam(gameResult.getHomeTeam())
                .awayTeam(gameResult.getAwayTeam())
                .ticketDate(gameDate)
                .seat(seat)
                .isWin(isWin)
                .createdDate(LocalDate.now())
                .gameResult(gameResult)
                .user(user)
                .build();
    }

    private List<TicketPreviewDTO> buildTicketPreviewDTO(List<Ticket> tickets) {
        List<TicketPreviewDTO> results = new ArrayList<>();

        // 1. ticketDate 기준 내림차순 정렬
        tickets.sort((t1, t2) -> t2.getTicketDate().compareTo(t1.getTicketDate()));

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
            throw new DuplicatedTicketException("티켓 이미지 업로드 실패");
        }
    }

    private GameResult findGameResult(OcrTicketDTO dto) {
        return gameResultRepository
                .findByAwayTeamAndGameDate(dto.getAwayTeam(), dto.getTicketDate())
                .orElseThrow(() -> new IllegalArgumentException(GAME_RESULT_NOT_FOUND_MESSAGE));
    }

    private String determineWinStatus(User user, GameResult gameResult) {
        Integer scoreHome = gameResult.getScoreHomeTeam();
        Integer scoreAway = gameResult.getScoreAwayTeam();

        if (scoreHome == null || scoreAway == null) return null;

        Team userTeam = user.getTeam();
        boolean isUserTeamInvolved =
                userTeam.equals(gameResult.getHomeTeam()) || userTeam.equals(gameResult.getAwayTeam());

        if (!isUserTeamInvolved) return null; // 내가 응원하는 팀이 경기에 없으면 무조건 null

        // 무승부
        if (scoreHome.equals(scoreAway)) {
            return "무";
        }

        // 승패 판단
        Team winnerTeam = (scoreHome > scoreAway) ? gameResult.getHomeTeam() : gameResult.getAwayTeam();
        return winnerTeam.equals(userTeam) ? "승" : "패";
    }

    private void validateDuplicateTicket(User user, GameResult gameResult, OcrTicketDTO dto) {
        boolean exists = ticketRepository.findByUserIdAndTicketDateAndHomeTeamAndAwayTeam(
                user.getId(), dto.getTicketDate(), gameResult.getHomeTeam(), gameResult.getAwayTeam()
        ).isPresent();

        if (exists) {
            throw new DuplicatedTicketException(DUPLICATED_TICKET_MESSAGE);
        }
    }

    private Integer calculateWinningStreak(User user) {
        List<Ticket> tickets = ticketRepository.findByUserIdAndFavoriteTeam(user.getId(), user.getTeam());

        // 내가 응원하는 팀의 티켓 등록이 아직 없다면 null로 처리
        if(tickets.isEmpty()) {
            return null;
        }

        // 내가 응원하는 팀의 티켓 중에서 isWin 기록이 null인 것은 제외
        List<Ticket> filterTickets = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if(ticket.getIsWin() != null) {
                filterTickets.add(ticket);
            }
        }

        for (Ticket filterTicket : filterTickets) {
            System.out.println(filterTicket.getTicketDate());
            System.out.println(filterTicket.getIsWin());
        }

        Integer equalCount = 0;
        Integer winCount = 1;
        Integer loseCount = -1;

        // 최신순 정렬
        filterTickets.sort((a, b) -> b.getTicketDate().compareTo(a.getTicketDate()));

        // 첫 번째로 나온 isWin 판별
        String firstIsWin = filterTickets.get(0).getIsWin();

        if(firstIsWin.equals("승")) {
            for (int i = 1; i < filterTickets.size(); i++) {
                String isWin = filterTickets.get(i).getIsWin();
                if(isWin.equals(firstIsWin)) {
                    winCount++;
                } else {
                    break;
                }
            }
            return winCount;
        } else if(firstIsWin.equals("패")) {
            for (int i = 1; i < filterTickets.size(); i++) {
                String isWin = filterTickets.get(i).getIsWin();
                if(isWin.equals(firstIsWin)) {
                    loseCount--;
                } else {
                    break;
                }
            }
            return loseCount;
        } else if(firstIsWin.equals("무")) {
            return equalCount;
        }
        return null;
    }
}
