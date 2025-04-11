package BallFan.entity.user;

import BallFan.entity.Team;
import BallFan.entity.Ticket;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "User")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "nickname")
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "team")
    private Team team;

    @Column(name = "image")
    private String image;

    @Column(name = "current_win_streak")
    private Integer currentWinStreak;

    @Column(name = "monthly_win_count")
    private int monthlyWinCount;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Ticket> tickets = new ArrayList<>();

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateTeam(Team team) {
        this.team = team;
    }

    public void updateImage(String image) {
        this.image = image;
    }

    public void updateCurrentWinStreak(Integer currentWinStreak) {
        this.currentWinStreak = currentWinStreak;
    }

}
