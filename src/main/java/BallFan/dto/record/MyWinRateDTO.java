package BallFan.dto.record;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MyWinRateDTO {

    private int winCount;
    private int drawCount;
    private int loseCount;
}
