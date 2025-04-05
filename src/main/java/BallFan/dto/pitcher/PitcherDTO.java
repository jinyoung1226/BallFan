package BallFan.dto.pitcher;


import BallFan.entity.Team;
import BallFan.entity.pitcher.PitcherType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PitcherDTO {

    private String name;
    private Team team;
    private PitcherType pitcherType;

}
