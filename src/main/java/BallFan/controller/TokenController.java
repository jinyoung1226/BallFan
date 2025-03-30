package BallFan.controller;

import BallFan.authentication.UserDetailsServiceImpl;
import BallFan.dto.Response.BaseResponse;
import BallFan.dto.auth.SignInDTO;
import BallFan.entity.user.User;
import BallFan.repository.TokenRepository;
import BallFan.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/token")
public class TokenController {
    private static final String AUTO_SIGN_IN_SUCCESS_MESSAGE = "자동 로그인 성공";

    private final TokenRepository tokenRepository;
    private final UserDetailsServiceImpl userDetailsService;
    private final TokenService tokenService;

    @PostMapping("/signIn")
    public ResponseEntity<SignInDTO> checkToken(@RequestHeader(name = "Authorization") String authorizationHeader) {
        Object userByContextHolder = userDetailsService.getUserByContextHolder();

        if (userByContextHolder instanceof User) {
            User user = (User) userByContextHolder;
            String refreshToken = tokenRepository.findById(user.getId());
            String accessToken = tokenService.resolveToken(authorizationHeader);

            return ResponseEntity.ok(SignInDTO.builder()
                    .id(user.getId())
                    .message(AUTO_SIGN_IN_SUCCESS_MESSAGE)
                    .refreshToken(refreshToken)
                    .accessToken(accessToken)
                    .build());
        } else {
            throw new IllegalArgumentException("유효하지 않은 사용자 타입입니다");
        }
    }

    @PostMapping("/re-issue")
    public ResponseEntity<BaseResponse> reIssueToken(@RequestHeader(name = "Authorization") String authorizationHeader) {
        tokenService.checkRefreshToken(authorizationHeader);
        SignInDTO signInDTO = tokenService.generateTokens();
        return ResponseEntity.ok(signInDTO);
    }

}
