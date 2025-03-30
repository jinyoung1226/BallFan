package BallFan.dto.auth;

import BallFan.entity.Team;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignUpRequest {
    private String email;
    private String password;
    private String nickname;
    private Team team;
}
