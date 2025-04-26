package programo._pro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import programo._pro.dto.userDto.UserDto;
import programo._pro.dto.userDto.UserCodingStatusDto;
import programo._pro.global.ApiResponse;
import programo._pro.service.UserService;

@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name = "User", description = "회원 정보 컨트롤러")
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    @Operation(summary = "회원정보 조회", description = "userId 값으로 해당 회원 정보 조회")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@Parameter(description = "회원의 id(PK)") @PathVariable int userId) {
        UserDto user = userService.getUserById(userId);

        if (user == null) {
            return ResponseEntity.ok(ApiResponse.fail("유저 정보를 불러오는데 실패했습니다"));
        }
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @Operation(summary = "코딩 상태 변경", description = "userId 값으로 해당 회원의 코딩 상태를 변경")
    @PatchMapping("/{userId}/coding-status")
    public ResponseEntity<ApiResponse<UserDto>> updateCodingStatus(
            @Parameter(description = "userId") @PathVariable Long userId,
            @RequestBody UserCodingStatusDto statusDto) {

        UserDto updatedUser = userService.updateUserCodingStatus(userId, statusDto.isCoding());

        if (updatedUser == null) {
            return ResponseEntity.ok(ApiResponse.fail("유저 코딩 상태 변경에 실패했습니다"));
        }
        return ResponseEntity.ok(ApiResponse.success(updatedUser));
    }
}
