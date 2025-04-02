package BallFan.entity;

import BallFan.entity.pitcher.PitcherStat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "GameResult")
public class GameResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_result_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "home_team")
    private Team homeTeam;

    @Enumerated(EnumType.STRING)
    @Column(name = "away_team")
    private Team awayTeam;

    @Column(name = "game_date")
    private LocalDate gameDate;

    @Column(name = "stadium")
    private String stadium;

    @Column(name = "score_home_team")
    private Integer scoreHomeTeam;

    @Column(name = "score_away_team")
    private Integer scoreAwayTeam;

    @OneToMany(mappedBy = "gameResult", fetch = FetchType.LAZY)
    private List<LineUp> lineUps = new ArrayList<>();

    @OneToMany(mappedBy = "gameResult", fetch = FetchType.LAZY)
    private List<PitcherStat> pitcherStats = new ArrayList<>();
}
