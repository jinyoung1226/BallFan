package BallFan.entity.pitcher;

import BallFan.entity.GameResult;
import BallFan.entity.Team;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "PitcherStat")
public class PitcherStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pitcher_stat_id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "team")
    private Team team;

    @Column(name = "pitcher_type")
    private PitcherType pitcherType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_result_id")
    private GameResult gameResult;
}
