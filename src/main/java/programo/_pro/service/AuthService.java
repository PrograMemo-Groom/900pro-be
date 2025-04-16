package programo._pro.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import programo._pro.dto.DupCheckDto;
import programo._pro.dto.UserInfo;
import programo._pro.entity.User;
import programo._pro.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public User signUp(UserInfo userInfo) {
        // 이메일 중복 확인
        boolean exists = userRepository.existsByEmail(userInfo.getEmail());
        if (exists) {
            throw new IllegalStateException("이미 사용 중인 이메일입니다.");
        }
        String encryptedPassword = passwordEncoder.encode(userInfo.getPassword());
        return userRepository.save(userInfo.toEntity(encryptedPassword));
    }


    @Transactional
    public boolean DupCheck(DupCheckDto checkDto) {
        // 이메일 중복 확인
        boolean exists = userRepository.existsByEmail(checkDto.getEmail());

        // 이미 존재한다면
        if (exists) {
            return false;
        }
        // 사용 가능한 이메일일 때
        return true;
    }
}

