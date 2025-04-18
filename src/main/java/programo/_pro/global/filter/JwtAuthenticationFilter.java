package programo._pro.global.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import programo._pro.dto.*;
import programo._pro.global.ApiResponse;
import programo._pro.global.ErrorResponse;
import programo._pro.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

/* UsernamePasswordAuthenticationFilter를 상속
주 목적: 사용자 로그인 인증 처리
로그인 요청("/api/v1/auth/login")에서 사용자 자격 증명(ID/비밀번호)을 확인
인증 성공 시 JWT 토큰 생성 및 발급
인증 실패 시 에러 응답 반환 */
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final JwtService jwtService;
    private final boolean postOnly = true;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
        setFilterProcessesUrl("/api/v1/auth/login"); // 이 필터가 처리할 url 직접 지정
    }

    // 로그인 시도에 대한 실제 인증 처리 로직
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        } else {
            try {
                SignInDto requestDto = new ObjectMapper().readValue(request.getInputStream(), SignInDto.class);
                String email = requestDto.getEmail();
                String password = requestDto.getPassword();

                // 받아온 이메일, 패스워드 정보를 이용해 이메일이 존재하고, 비밀번호가 일치하면 성공 -> 성공시 인증정보가 담긴 객체 반환
                return getAuthenticationManager().authenticate(new UsernamePasswordAuthenticationToken(email, password));

            } catch (IOException e) {
                // 추후 구체화 된 예외로 변경
                throw new RuntimeException(e);
            }
        }
    }

    // 로그인 성공 시
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        String email = ((AuthenticationToken) authResult.getPrincipal()).getUsername();
        String token = jwtService.createToken(new JwtUserInfoDto(email));
        ApiResponse<String> responseMessage = ApiResponse.success(token);
        String responseJSON = new ObjectMapper().writeValueAsString(responseMessage);
        response.setContentType("application/json; charset=UTF-8"); // JSON 타입 + UTF-8 설정
        response.setCharacterEncoding("UTF-8"); // 한글 인코딩 설정 추가
        response.getWriter().write(responseJSON);
    }

    // 로그인 실패 시
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        ErrorResponse errorResponse = ErrorResponse.of(HttpStatus.UNAUTHORIZED.value(), failed.getMessage(), "/login", "id and password invalid");
        String responseJSON = new ObjectMapper().writeValueAsString(errorResponse);
        response.setContentType("application/json; charset=UTF-8"); // JSON 타입 + UTF-8 설정
        response.setCharacterEncoding("UTF-8"); // 한글 인코딩 설정 추가
        response.getWriter().write(responseJSON);
    }

}
