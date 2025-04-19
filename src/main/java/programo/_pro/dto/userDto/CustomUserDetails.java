package programo._pro.dto.userDto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
    private final UserInfo userInfo;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<String> roles = new ArrayList<>();
        roles.add("ROLE_USER");

        return roles.stream()
            .map(SimpleGrantedAuthority::new)
            .toList();
    }

    @Override
    public String getPassword() {
        return userInfo.getPassword();
    }

    /**
     * UserDetails 인터페이스 요구사항, 실제로는 이메일 반환
     */
    @Override
    public String getUsername() {
        return userInfo.getEmail();
    }

    /**
     * 이메일을 반환하는 편의 메서드
     * getUsername()과 동일한 값 반환
     */
    public String getEmail() {
        return userInfo.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
