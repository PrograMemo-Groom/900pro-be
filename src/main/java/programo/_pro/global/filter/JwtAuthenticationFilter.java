package programo._pro.global.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import programo._pro.dto.AuthenticationToken;
import programo._pro.dto.JwtUserInfoDto;
import programo._pro.dto.authDto.SignInDto;
import programo._pro.entity.User;
import programo._pro.global.ApiResponse;
import programo._pro.global.ErrorResponse;
import programo._pro.global.exception.userException.UserException;
import programo._pro.repository.UserRepository;
import programo._pro.service.JwtService;

import java.io.IOException;

/* UsernamePasswordAuthenticationFilter를 상속
주 목적: 사용자 로그인 인증 처리
로그인 요청("/api/v1/auth/login")에서 사용자 자격 증명(ID/비밀번호)을 확인
인증 성공 시 JWT 토큰 생성 및 발급
인증 실패 시 에러 응답 반환 */
@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final boolean postOnly = true;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;

        setFilterProcessesUrl("/api/auth/login"); // 이 필터가 처리할 url 직접 지정
    }

    // 로그인 시도 시 자동 호출하고 인증 로직 처리 로직 실행
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        // 디버깅 추가
        log.debug("Request {}, Response {}", request, request);

        // Preflight 요청은 바로 pass
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            log.info("Preflight 요청, 인증 시도하지 않음");
            return null; // 이러면 doFilterChain에서 인증 실패로 빠지지 않음
        }
        if (postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        } else {
            try {
                SignInDto requestDto = new ObjectMapper().readValue(request.getInputStream(), SignInDto.class);
                String email = requestDto.getEmail();
                String password = requestDto.getPassword();

                log.info("56: @@@@@@@@@@@@@@ Email: {}, Password: {}", email, password);

                User user = userRepository.findByEmail(email).orElseThrow(() -> new AuthenticationServiceException("존재하지 않는 사용자입니다.")); // 로그인 실패 예외 던짐


                log.info("@@@@@@@@@@@@@@ User: {}", user);
                if (!user.isActive()) {
                    log.error("63 : error발생!!!!!!!!@@!@#!@#!@#!@#!@#");
                    throw new AuthenticationServiceException("탈퇴한 계정이거나 존재하지 않습니다."); // 로그인 실패 예외 던짐
                }

                // 받아온 이메일, 패스워드 정보를 이용해 이메일이 존재하고, 비밀번호가 일치하면 성공 -> 성공시 인증정보가 담긴 객체 반환
                return getAuthenticationManager().authenticate(new UsernamePasswordAuthenticationToken(email, password));

            } catch (IOException e) {
                log.error("71 :" + e.getMessage());
                // 추후 구체화 된 예외로 변경
                throw new RuntimeException(e);
            } catch (AuthenticationException e) { // 로그인 실패 예외를 잡음
                // 여기서 실패 처리 메소드를 직접 호출하고 null 반환
                log.error("76 :" + e.getMessage());
                try {
                    unsuccessfulAuthentication(request, response, e); // 실페 처리 메서드 호출
                } catch (IOException ex) {
                    throw new RuntimeException(ex); // 그럼에도 못잡았으면 런타임 예외 던짐
                }
                return null;
            }
        }
    }

    // 로그인 성공 시 스프링 시큐리티 내부에서 자동 호출
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

        String email = ((AuthenticationToken) authResult.getPrincipal()).getUsername();
        User user = userRepository.findByEmail(email).orElseThrow(UserException::byEmail);
        Long userId = user.getId();

        log.info("여기까지 왔으면 로그인 성공이야!!!!");

        String token = jwtService.createToken(new JwtUserInfoDto(userId ,email)); // 이메일을 이용해 JWT Token 생성

        // 이 부분에서 사용자 정보가 담긴 토큰이 응답객체에 담김!@@#!#@!@@@@@@@
        ApiResponse<String> responseMessage = ApiResponse.success(token, "로그인에 성공했습니다");

        // 응답 객체 설정
        String responseJSON = new ObjectMapper().writeValueAsString(responseMessage);
        response.setContentType("application/json; charset=UTF-8"); // JSON 타입 + UTF-8 설정
        response.setCharacterEncoding("UTF-8"); // 한글 인코딩 설정 추가
        response.getWriter().write(responseJSON);
    }

    // 로그인 실패 시 스프링 시큐리티 내부에서 자동 호출
    // 여기서 로그인 예외를 잡아서 ErrorResponse 객체로 응답
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        // 로그인 실패
        log.error("@@@@@@@@@@@@ FAILED" + failed.getMessage());


        ErrorResponse errorResponse = ErrorResponse.of(HttpStatus.UNAUTHORIZED.value(), failed.getMessage(), "/login", "아이디와 비밀번호가 올바르지 않습니다.");
        String responseJSON = new ObjectMapper().writeValueAsString(errorResponse);
        response.setContentType("application/json; charset=UTF-8"); // JSON 타입 + UTF-8 설정
        response.setCharacterEncoding("UTF-8"); // 한글 인코딩 설정 추가
        response.getWriter().write(responseJSON);
    }

}
