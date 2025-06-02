package BallFan.dto.record;

import lombok.*;

@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRankingDTO {

    private Long id;
    private String nickname;
    private String image;
    private int winCount;
    private int lossCount;
    private int drawCount;
    private int rank;
}
