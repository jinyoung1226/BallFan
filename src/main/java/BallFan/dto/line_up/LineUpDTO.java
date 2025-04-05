package BallFan.dto.line_up;

import BallFan.entity.Team;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LineUpDTO {

    private String name;
    private int order;
    private String position;
    private Team team;

}
