package programo._pro.dto;

import programo._pro.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class AuthenticationToken implements UserDetails {
    private final Long id; // userId
    private final String email;
    private final String password;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return password;
    }

    /**
     * UserDetails 인터페이스 요구사항, 실제로는 이메일 반환
     */
    @Override
    public String getUsername() {
        return email;
    }

    public Long getId(){ return id; }

    /**
     * 이메일을 반환하는 편의 메서드
     * getUsername()과 동일한 값 반환
     */
    public String getEmail() {
        return email;
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

    public static AuthenticationToken of(User u) {
        return new AuthenticationToken(u.getId(), u.getEmail(), u.getPassword());
    }
}
