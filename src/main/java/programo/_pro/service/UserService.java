package programo._pro.service;

import programo._pro.dto.AuthenticationToken;
import programo._pro.dto.userDto.UserDto;
import programo._pro.entity.User;
import programo._pro.entity.TeamMember;
import programo._pro.global.exception.userException.UserException;
import programo._pro.repository.UserRepository;
import programo._pro.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(UserException::byEmail);
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
                .orElseThrow(UserException::byId);

        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .isActive(user.isActive())
                .username(user.getUsername())
                .isCoding(user.isCoding())
                .build();

        List<TeamMember> teamMembers = teamMemberRepository.findByUserId(user.getId());
        if (!teamMembers.isEmpty()) {
            TeamMember teamMember = teamMembers.get(0);
            userDto.setTeamId(teamMember.getTeam().getId());
            userDto.setIsTeamLeader(teamMember.isLeader());
        }

        return userDto;
    }

    @Transactional
    public UserDto updateUserCodingStatus(Long userId, boolean isCoding) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserException::byId);

        user.setCoding(isCoding);
        userRepository.save(user);

        return getUserById(userId.intValue());
    }
}
