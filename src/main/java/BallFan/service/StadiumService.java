package BallFan.service;

import BallFan.authentication.UserDetailsServiceImpl;
import BallFan.dto.stadium.StadiumVisitDTO;
import BallFan.entity.StadiumVisit;
import BallFan.entity.user.User;
import BallFan.exception.stadium.StadiumVisitNotFoundException;
import BallFan.repository.StadiumVisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StadiumService {

    private static final String STADIUM_VISIT_NOT_FOUND_MESSAGE = "방문한 경기장이 없습니다";
    private final UserDetailsServiceImpl userDetailsService;
    private final StadiumVisitRepository stadiumVisitRepository;

    public List<StadiumVisitDTO> getStadiumVisits() {
        User user = userDetailsService.getUserByContextHolder();

        List<StadiumVisit> stadiumVisits = stadiumVisitRepository.findByUserId(user.getId());
        if(stadiumVisits.isEmpty()) {
            throw new StadiumVisitNotFoundException(STADIUM_VISIT_NOT_FOUND_MESSAGE);
        }

        return buildStadiumVisitDTO(stadiumVisits);
    }

    private List<StadiumVisitDTO> buildStadiumVisitDTO(List<StadiumVisit> stadiumVisits) {
        List<StadiumVisitDTO> stadiumVisitDTOS = new ArrayList<>();

        for(StadiumVisit stadiumVisit : stadiumVisits) {
            stadiumVisitDTOS.add(new StadiumVisitDTO(stadiumVisit.getStadium(), stadiumVisit.getVisitCount()));
        }

        return stadiumVisitDTOS;
    }
}
