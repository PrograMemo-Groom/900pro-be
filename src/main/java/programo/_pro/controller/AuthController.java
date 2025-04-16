package programo._pro.controller;

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
public class AuthController {
    private final AuthService authService;

    @PostMapping("/join")
    public ResponseEntity<ApiResponse<User>> signUp(@RequestBody @Valid SignUpDto signUpDto) {
        return ResponseEntity.ok(ApiResponse.success(authService.signUp(signUpDto.toService())));
    }
}
