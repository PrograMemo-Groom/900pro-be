package programo._pro.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import programo._pro.dto.userDto.UserDto;
import programo._pro.dto.userDto.UserUpdateRequestDto;
import programo._pro.entity.User;
import programo._pro.global.exception.userException.NotFoundUserException;
import programo._pro.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyPageService {
    private final UserRepository userRepository;

    // 유저 id를 이용해 회원정보 조회
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
}
