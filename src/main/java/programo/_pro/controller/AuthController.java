package programo._pro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import programo._pro.dto.DupCheckDto;
import programo._pro.dto.SignInDto;
import programo._pro.dto.SignUpDto;
import programo._pro.entity.User;
import programo._pro.global.ApiResponse;
import programo._pro.repository.UserRepository;
import programo._pro.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "사용자 인증 API")
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

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

    @Operation(summary = "이메일 중복 체크", description = "이메일의 중복 여부를 확인합니다")
    @PostMapping("/dupCheck")
    public ResponseEntity<ApiResponse<String>> dupCheck(@RequestBody @Valid DupCheckDto dupCheckDto) {
        boolean exists =  authService.DupCheck(dupCheckDto);

        // 만약 이메일이 이미 존재하지 않는다면
        if (exists) { // true 면 사용 가능한 이메일
            return ResponseEntity.status(200).body(ApiResponse.success(dupCheckDto.getEmail(), "사용 가능한 이메일입니다"));

        } else {
            return ResponseEntity.status(400).body(ApiResponse.fail("이미 사용 중인 이메일입니다."));
        }
    }

    // 비밀번호 초기화 후 임시비밀번호를 입력해서 로그인 시도할 때 값 검증 메서드
    @PostMapping("/auth")
    public ResponseEntity<ApiResponse<String>> authenticate(@RequestBody @Valid SignInDto signInDto) {
        boolean isMatch = authService.authenticate(signInDto.getEmail(), signInDto.getPassword());

        if (isMatch) {
            return ResponseEntity.ok(ApiResponse.success(signInDto.getEmail(), "성공"));
        } else {
            return ResponseEntity.ok(ApiResponse.fail("아이디와 비밀번호 값을 다시 확인하세요"));
        }
    }


}