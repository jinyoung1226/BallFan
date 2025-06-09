package BallFan.service;

import BallFan.authentication.JwtProvider;
import BallFan.authentication.UserDetailsServiceImpl;
import BallFan.dto.auth.SignInDTO;
import BallFan.dto.auth.SignInRequest;
import BallFan.dto.auth.SignUpRequest;
import BallFan.entity.user.User;
import BallFan.entity.token.BlackList;
import BallFan.exception.auth.DuplicatedSignUpException;
import BallFan.repository.BlackListRepository;
import BallFan.repository.TokenRepository;
import BallFan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsServiceImpl userDetailsService;
    private final TokenRepository tokenRepository;
    private final TokenService tokenService;
    private final BlackListRepository blackListRepository;
    private final UserRepository userRepository;

    @Transactional
    public void signUp(SignUpRequest request){
        try {
            User user = User.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .nickname(request.getNickname())
                    .image("https://b-cube-web.s3.ap-northeast-2.amazonaws.com/b-cube-web/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA+2025-06-10+%E1%84%8B%E1%85%A9%E1%84%8C%E1%85%A5%E1%86%AB+1.19.01.png")
                    .team(request.getTeam())
                    .build();
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicatedSignUpException("이미 존재하는 이메일입니다.");
        }
    }


    public SignInDTO signIn(SignInRequest request) {
        System.out.println(request.getEmail() + " " + request.getPassword());

        // 이메일 존재 여부 체크
        User user1 = userRepository.findUserByEmail(request.getEmail())
                .orElseThrow(() -> new DuplicatedSignUpException("존재하지 않는 이메일입니다."));

        Authentication authentication = authenticate(request);
        System.out.println(authentication.getPrincipal());
        String accessToken = jwtProvider.generateAccessToken(authentication);
        String refreshToken = jwtProvider.generateRefreshToken(authentication);

        Object principal = authentication.getPrincipal();
        Long userId = null;
        String message = "로그인 성공";

        if (principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername();
            System.out.println(email);

            User user = userRepository.findUserByEmail(email).orElse(null);
            if (user != null) {
                userId = user.getId();
                tokenRepository.save(userId, refreshToken);
            }
        }

        return SignInDTO.builder()
                .id(userId)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .message(message)
                .build();
    }

    public void signOut(String authorizationHeader) {
        User user = userDetailsService.getUserByContextHolder();
        System.out.println(user);
        String refreshToken = tokenService.resolveToken(authorizationHeader);
        System.out.println(refreshToken);
        tokenRepository.deleteId(user.getId());
        saveBlackList(refreshToken);
    }

    private void saveBlackList(String refreshToken) {
        try {
            blackListRepository.save(BlackList.builder()
                    .refreshToken(refreshToken)
                    .build());
        } catch (DuplicateKeyException e) {
            log.info("이미 로그아웃 처리한 토큰: " + refreshToken);
        }
    }

    private Authentication authenticate(SignInRequest request) {
        Authentication authenticationToken = new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
        );
        return authenticationManagerBuilder.getObject().authenticate(authenticationToken);
    }
}
