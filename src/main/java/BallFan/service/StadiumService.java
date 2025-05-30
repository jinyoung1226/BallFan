package BallFan.service;

import BallFan.authentication.UserDetailsServiceImpl;
import BallFan.dto.stadium.StadiumVisitDTO;
import BallFan.entity.StadiumVisit;
import BallFan.entity.user.User;
import BallFan.repository.StadiumVisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StadiumService {

    private static final String STADIUM_VISIT_NOT_FOUND_MESSAGE = "방문한 경기장이 없습니다";
    private final UserDetailsServiceImpl userDetailsService;
    private final StadiumVisitRepository stadiumVisitRepository;

    public List<StadiumVisitDTO> getStadiumVisits() {
        User user = userDetailsService.getUserByContextHolder();
        List<StadiumVisit> stadiumVisits = stadiumVisitRepository.findByUserId(user.getId());

        // Stadium 이름 기준으로 Map 생성
        Map<String, Integer> visitMap = new HashMap<>();
        for (StadiumVisit visit : stadiumVisits) {
            visitMap.put(visit.getStadium(), visit.getVisitCount());
        }

        // 전체 9개 경기장 리스트 (예시)
        List<String> allStadiums = List.of(
                "서울 잠실야구장",
                "서울 고척스카이돔",
                "인천 SSG 랜더스 필드",
                "수원 KT 위즈 파크",
                "대전 한화생명 볼 파크",
                "광주 기아 챔피언스 필드",
                "대구 삼성 라이온즈 파크",
                "창원 NC 파크",
                "부산 사직야구장"
        );


        List<StadiumVisitDTO> result = new ArrayList<>();
        for (String stadium : allStadiums) {
            int count = visitMap.getOrDefault(stadium, 0);
            result.add(new StadiumVisitDTO(stadium, count));
        }

        return result;
    }
}
