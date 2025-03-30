package BallFan.authentication;

import java.util.Collection;
import java.util.Collections;

import BallFan.entity.user.User;
import BallFan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private static final String USER_NOT_FOUND_EXCEPTION = "존재하지 않는 회원입니다.";

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_EXCEPTION));
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), getAuthorities(user));
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Object user) {
        // 권한 정보를 GrantedAuthority 객체 컬렉션으로 반환
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    public User getUserByContextHolder() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserDetails userDetails = (UserDetails) principal;
        return userRepository.findUserByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_EXCEPTION));
    }

    public Authentication getAuthentication(){
        return SecurityContextHolder.getContext().getAuthentication();
    }
}

