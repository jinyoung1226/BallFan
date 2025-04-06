package BallFan.dto.stadium;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StadiumVisitDTO {

    private String stadium;
    private int visitCount;

}
