package BallFan.dto.user;

import BallFan.entity.Team;
import BallFan.entity.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private Long id;
    private String nickname;
    private Team team;
    private String image;
    private int currentWinStreak;
    private int monthlyWinCount;

    public static UserDTO from(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .image(user.getImage())
                .team(user.getTeam())
                .currentWinStreak(user.getCurrentWinStreak())
                .monthlyWinCount(user.getMonthlyWinCount())
                .build();
    }

}
