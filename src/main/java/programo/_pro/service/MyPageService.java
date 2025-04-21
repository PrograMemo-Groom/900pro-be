package programo._pro.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import programo._pro.dto.userDto.UserDto;
import programo._pro.dto.userDto.UserInfoUpdateDto;
import programo._pro.entity.User;
import programo._pro.global.exception.userException.NotFoundUserException;
import programo._pro.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyPageService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // 유저 id를 이용해 회원정보 조회
    @Transactional(readOnly = true)
    public UserDto getUser(int userId) {

        // Id값으로 찾을 때 조회 실패시 예외 throw
        User user = userRepository.findById((long) userId).orElseThrow(NotFoundUserException::byId);


        // 응답용 객체에 데이터 세팅(비밀번호 포함x)
        UserDto userDto = UserDto.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .build();

        log.info("userDto : {}", userDto.toString());

        return userDto;
    }

    // 유저 정보를 입력받은 데이터로 업데이트 합니다
    @Transactional
    public void updateUserInfo(int userId, UserInfoUpdateDto userInfoUpdateDto) {
        // 정보를 수정할 유저 객체 가져옴
        User user = userRepository.findById((long) userId).orElseThrow(NotFoundUserException::byId);

        // 입력한 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(userInfoUpdateDto.getPassword());

        // 입력받은 데이터로 이름과 비밀번호 업데이트
        user.setUsername(userInfoUpdateDto.getUsername());
        user.setPassword(encodedPassword); // 암호화된 데이터

        // 해당 유저의 정보 업데이트
        userRepository.save(user);
    }

    public void deleteUser(int userId) {
        // 정보를 수정할 유저 객체 가져옴
        User user = userRepository.findById((long) userId).orElseThrow(NotFoundUserException::byId);

        // 회원 정보 비활성화 상태로 변경
        user.setActive(false);

        // 변경된 상태 업데이트
        userRepository.save(user);
    }
}
