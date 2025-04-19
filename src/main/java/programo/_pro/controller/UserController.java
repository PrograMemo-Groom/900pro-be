package programo._pro.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import programo._pro.dto.UserDto;
import programo._pro.entity.User;
import programo._pro.global.ApiResponse;
import programo._pro.service.UserService;

@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name = "User", description = "회원 정보 컨트롤러")
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;


    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable int userId) {
        UserDto user = userService.getUserById(userId);

        if (user == null) {
            return ResponseEntity.ok(ApiResponse.fail("유저 정보를 불러오는데 실패했습니다"));
        }
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}
