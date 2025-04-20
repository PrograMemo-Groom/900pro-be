package programo._pro.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import programo._pro.dto.userDto.UserDto;
import programo._pro.dto.userDto.UserInfoUpdateDto;
import programo._pro.global.ApiResponse;
import programo._pro.service.MyPageService;

@RestController
@RequestMapping("/api/mypage")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "MyPage", description = "회원 정보 수정 페이지")
public class MyPageController {
    private final MyPageService myPageService;


    // 이름, 이메일을 가져옴
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDto>> getUser(@PathVariable int userId) {
        UserDto userDto =  myPageService.getUser(userId);

        return ResponseEntity.ok(ApiResponse.success(userDto, String.format("userId : %d 의 회원 정보를 조회했습니다.", userId)));
    }


    // 유저 id 를 받고, body 로 들어온 정보로 업데이트
    @PatchMapping("/update/{userId}")
    public ResponseEntity<ApiResponse<String>> updateUserInfo(@PathVariable int userId, @RequestBody @Valid UserInfoUpdateDto userInfoUpdateDto) {
         myPageService.updateUserInfo(userId, userInfoUpdateDto);

         return ResponseEntity.ok(ApiResponse.success("success", "성공적으로 회원정보를 업데이트했습니다."));
    }


}
