package programo._pro.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import programo._pro.dto.EmailVerficationRequest;
import programo._pro.dto.mailDto.EmailRequest;
import programo._pro.global.ApiResponse;
import programo._pro.service.MailService;
import programo._pro.service.UserService;

import java.util.HashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mail")
public class MailController {
    private final MailService mailService;
    private final UserService userService;
    // 인증 이메일 전송
    @PostMapping("/mailSend")
    @Operation(summary = "인증 이메일 전송", description = "입력받은 이메일로 인증코드를 전송합니다")
    public ResponseEntity<ApiResponse<HashMap<String, Object>>> mailSend(@RequestBody EmailRequest email) {
        HashMap<String, Object> map = new HashMap<>();

        try {
            mailService.sendMail(email);
            map.put("success", Boolean.TRUE);
        } catch (Exception e) {
            map.put("success", Boolean.FALSE);
            map.put("error", e.getMessage());
        }

        return ResponseEntity.ok(ApiResponse.success(map, "인증 이메일이 정상적으로 전송되었습니다."));
    }

    // 인증번호 일치여부 확인
    @PostMapping("/mailCheck")
    @Operation(summary = "인증코드 검증", description = "이메일로 받은 인증코드와 일치하는 지 검증합니다.")
    public ResponseEntity<ApiResponse<String>> mailCheck(@RequestBody EmailVerficationRequest emailVerificationRequest) {

        boolean is_match = mailService.mailCheck(emailVerificationRequest.getEmail(), emailVerificationRequest.getCode());

        if(is_match) {
            return ResponseEntity.ok(ApiResponse.success("success", "인증코드가 일치합니다."));
        } else {
            return ResponseEntity.ok(ApiResponse.fail("인증코드가 일치하지 않습니다."));
        }
    }

    @PostMapping("/reset-password")
    @Operation(summary = "초기화 비밀번호 전송", description = "입력받은 이메일이 존재하는지 확인하고, 초기화 비밀번호를 제공합니다.")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody EmailRequest emailVerificationRequest) {

        boolean isExist = mailService.resetPassword(emailVerificationRequest.getEmail());

        if(isExist) {
            return ResponseEntity.ok(ApiResponse.success("success", "입력한 이메일로 임시 비밀번호를 전송했습니다."));
        }
        return ResponseEntity.ok(ApiResponse.fail("fail", "입력한 이메일은 가입되어있지 않습니다."));
    }
}
