package programo._pro.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import programo._pro.dto.AuthenticationToken;
import programo._pro.entity.User;
import programo._pro.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 이메일을 기준으로 user 정보를 조회하고, 없으면 예외 발생
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일의 사용자를 찾을 수 없습니다: " + email));

        // 조회된 유저의 id, email, password를 담은 커스텀 UserDetails 객체(AuthenticationToken)를 반환
        // 이 객체는 Spring Security의 인증 과정에서 사용자 정보로 사용됨
        return new AuthenticationToken(user.getId(), user.getEmail(), user.getPassword());
    }
}
