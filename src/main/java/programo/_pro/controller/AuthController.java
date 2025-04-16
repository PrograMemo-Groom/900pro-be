package programo._pro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import programo._pro.dto.SignInDto;
import programo._pro.dto.SignUpDto;
import programo._pro.entity.User;
import programo._pro.global.ApiResponse;
import programo._pro.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "사용자 인증 API")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "회원가입", description = "이메일, 이름, 비밀번호를 입력해 회원가입을 합니다")
    @PostMapping("/join")
    public ResponseEntity<ApiResponse<User>> signUp(@RequestBody @Valid SignUpDto signUpDto) {
        return ResponseEntity.ok(ApiResponse.success(authService.signUp(signUpDto.toService())));
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인을 시도하고 JWT 토큰을 발급 받습니다")
    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "로그인 요청") SignInDto signInDto) {
        // 실제 동작은 필터에서 처리, Swagger 문서용 코드
        return ResponseEntity.ok().build();
    }
}
