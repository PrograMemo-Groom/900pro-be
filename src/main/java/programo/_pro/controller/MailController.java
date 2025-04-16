package programo._pro.controller;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import programo._pro.dto.EmailRequest;
import programo._pro.global.ApiResponse;
import programo._pro.service.MailService;

import java.util.HashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mail")
public class MailController {
    private final MailService mailService;
    private int number; // 이메일 인증 숫자를 저장하는 변수

    // 인증 이메일 전송
    @PostMapping("/mailSend")
    public ResponseEntity<ApiResponse<HashMap<String, Object>>> mailSend(@RequestBody EmailRequest email) {
        HashMap<String, Object> map = new HashMap<>();

        try {
            number = mailService.sendMail(email);
            String num = String.valueOf(number);

            map.put("success", Boolean.TRUE);
            map.put("number", num);
        } catch (Exception e) {
            map.put("success", Boolean.FALSE);
            map.put("error", e.getMessage());
        }

        return ResponseEntity.ok(ApiResponse.success(map, "인증 이메일이 정상적으로 전송되었습니다."));
    }

    // 인증번호 일치여부 확인
    @GetMapping("/mailCheck")
    public ResponseEntity<?> mailCheck(@RequestParam String userNumber) {

        boolean isMatch = userNumber.equals(String.valueOf(number));

        return ResponseEntity.ok(isMatch);
    }
}
