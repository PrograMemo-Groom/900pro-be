package programo._pro.service;

import programo._pro.dto.AuthenticationToken;
import programo._pro.dto.UserDto;
import programo._pro.entity.User;
import programo._pro.global.exception.NotFoundUserException;
import programo._pro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(NotFoundUserException::new);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("해당하는 유저는 없습니다."));

        return AuthenticationToken.of(user);
    }

    public UserDto getUserById(int userId) {
        User user = userRepository.findById((long) userId)
                .orElseThrow(NotFoundUserException::new);

        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .isActive(user.isActive())
                .username(user.getUsername())
                .build();

        return userDto;
    }
}
