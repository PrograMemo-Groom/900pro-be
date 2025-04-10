package programo._pro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import programo._pro.dto.SignInDto;
import programo._pro.dto.SignUpDto;
import programo._pro.dto.UserInfo;
import programo._pro.entity.User;
import programo._pro.service.AuthService;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 인증 컨트롤러 테스트
 *
 * 이 테스트 클래스는 인증 컨트롤러 및 JWT 토큰 발급 기능을 검증합니다.
 * AuthController가 로그인 성공 시 JWT 토큰을 발급하고, 회원가입 시 적절한 사용자 정보를 반환하는지 테스트합니다.
 *
 * Mockito 프레임워크를 사용하여 AuthService를 모킹하고, MockMvc를 통해 인증 컨트롤러의 엔드포인트를 테스트합니다.
 *
 * 테스트 내용:
 * - 기본 로그인 성공 테스트
 * - 회원가입 성공 테스트
 * - JWT 토큰 발급 및 검증 테스트
 * - 회원가입 후 사용자 정보 반환 테스트
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 테스트용 상수
    private final String TEST_JWT_EMAIL = "test@example.com";
    private final String TEST_JWT_PASSWORD = "password123";
    // private final String TEST_JWT_USERNAME = "testuser";
    private final String TEST_JWT_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6InRlc3RAZXhhbXBsZS5jb20iLCJleHAiOjIxNzc0NTI3OTl9.RyQ8ftUDU94xg-_Z3Bnhh2c9s8rwtKVMtsCHSGQTKdY";
    private final String TEST_NEW_EMAIL = "newuser@example.com";
    private final String TEST_NEW_USERNAME = "newuser";
    private final String TEST_NEW_PASSWORD = "newpassword";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    void loginSuccessTest() throws Exception {
        // given
        String testToken = "test.token";

        SignInDto signInDto = new SignInDto();
        signInDto.setEmail(TEST_JWT_EMAIL);
        signInDto.setPassword(TEST_JWT_PASSWORD);

        // when
        when(authService.signIn(eq(TEST_JWT_EMAIL), eq(TEST_JWT_PASSWORD))).thenReturn(testToken);

        // then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signInDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(testToken));
    }

    @Test
    @DisplayName("회원가입 성공 테스트")
    void signUpSuccessTest() throws Exception {
        // given
        SignUpDto signUpDto = new SignUpDto();
        signUpDto.setEmail(TEST_NEW_EMAIL);
        signUpDto.setUsername(TEST_NEW_USERNAME);
        signUpDto.setPassword(TEST_NEW_PASSWORD);

        User mockUser = User.builder()
                .email(TEST_NEW_EMAIL)
                .username(TEST_NEW_USERNAME)
                .password(TEST_NEW_PASSWORD)
                .flag(1)
                .build();

        // when
        when(authService.signUp(any(UserInfo.class))).thenReturn(mockUser);

        // then
        mockMvc.perform(post("/api/v1/auth/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    /**
     * 로그인 성공 시 JWT 토큰이 발급되는지 테스트
     *
     * 이 테스트는 사용자가 유효한 이메일과 비밀번호로 로그인할 경우,
     * 인증 컨트롤러가 AuthService를 통해 JWT 토큰을 발급하는지 확인합니다.
     * 응답에 JWT 토큰이 포함되어 있는지 검증합니다.
     */
    @Test
    @DisplayName("로그인 성공 시 JWT 토큰이 발급되는지 테스트")
    void loginShouldReturnJwtToken() throws Exception {
        // given
        SignInDto signInDto = new SignInDto();
        signInDto.setEmail(TEST_JWT_EMAIL);
        signInDto.setPassword(TEST_JWT_PASSWORD);

        // AuthService가 JwtService를 통해 토큰을 생성하는 것을 모킹
        when(authService.signIn(TEST_JWT_EMAIL, TEST_JWT_PASSWORD)).thenReturn(TEST_JWT_TOKEN);

        // when & then
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signInDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(TEST_JWT_TOKEN))
                .andReturn();

        // 응답에서 JWT 토큰을 추출
        String responseContent = result.getResponse().getContentAsString();
        assertTrue(responseContent.contains(TEST_JWT_TOKEN), "응답에 JWT 토큰이 포함되어 있어야 합니다.");
    }

    /**
     * 회원가입 성공 후 사용자 정보가 반환되는지 테스트
     *
     * 이 테스트는 사용자가 회원가입을 성공적으로 완료한 후,
     * 인증 컨트롤러가 사용자 정보를 응답으로 올바르게 반환하는지 확인합니다.
     * 반환된 사용자 정보에 이메일과 사용자 이름이 정확히 포함되어 있는지 검증합니다.
     */
    @Test
    @DisplayName("회원가입 성공 후 사용자 정보가 반환되는지 테스트")
    void signUpShouldReturnUserInfo() throws Exception {
        // given
        User mockUser = User.builder()
                .email(TEST_NEW_EMAIL)
                .username(TEST_NEW_USERNAME)
                .password(TEST_NEW_PASSWORD)
                .flag(1)
                .build();

        SignUpDto signUpDto = new SignUpDto();
        signUpDto.setEmail(TEST_NEW_EMAIL);
        signUpDto.setUsername(TEST_NEW_USERNAME);
        signUpDto.setPassword(TEST_NEW_PASSWORD);

        // AuthService를 모킹하여 사용자 생성 로직을 시뮬레이션
        when(authService.signUp(any(UserInfo.class))).thenReturn(mockUser);

        // when & then
        mockMvc.perform(post("/api/v1/auth/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(TEST_NEW_EMAIL))
                .andExpect(jsonPath("$.data.username").value(TEST_NEW_USERNAME));
    }
}
