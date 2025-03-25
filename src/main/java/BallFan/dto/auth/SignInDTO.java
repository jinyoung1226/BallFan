package BallFan.dto.auth;

import BallFan.dto.Response.BaseResponse;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class SignInDTO extends BaseResponse {
    private Long id;
    private String accessToken;
    private String refreshToken;
    private String message;
}