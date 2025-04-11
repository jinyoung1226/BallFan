package BallFan.dto.record;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRankingDTO {

    private Long id;
    private String nickname;
    private Integer currentWinStreak;
    private int rank;
}
