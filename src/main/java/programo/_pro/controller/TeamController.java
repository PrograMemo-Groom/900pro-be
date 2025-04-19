package programo._pro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import programo._pro.dto.TeamCardDto;
import programo._pro.dto.TeamCreateRequest;
import programo._pro.dto.TeamMainDto;
import programo._pro.service.TeamService;

import java.util.List;


@Tag(name = "Team", description = "팀 관련 API")
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;


    // 팀 리스트 조회
    @Operation(
            summary = "팀 리스트 조회",
            description = "모든 팀의 간단한 정보를 리스트 형식으로 조회합니다. 검색어, 난이도, 정렬 기준 적용 가능.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "팀 리스트 조회 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 팀"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    @GetMapping
    public ResponseEntity<List<TeamCardDto>> getAllTeams(
            @Parameter(description = "검색 키워드") @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "팀 난이도 필터 (all, 상, 중, 하)") @RequestParam(value = "level", required = false) String level,
            @Parameter(description = "일반 정렬 필터 (problemCount, name, createdAt, currentMembers)") @RequestParam(value = "sort", required = false) String sort
    ) {
        List<TeamCardDto> teamCards = teamService.getAllTeams(keyword, level, sort);
        return ResponseEntity.ok(teamCards);
    }


    // 팀 상세 정보 & 팀원 정보 조회
    @Operation(
            summary = "팀 상세 정보 & 팀원 정보 조회",
            description = "팀의 상세 정보와 팀원 목록(권한 포함)을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "팀 리스트 조회 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 팀"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    @GetMapping("/{teamId}")
    public ResponseEntity<TeamMainDto> getTeamMain(
            @Parameter(description = "팀 ID") @PathVariable("teamId") Long teamId
    ) {
        TeamMainDto teamMainDto = teamService.getTeamMain(teamId);
        return ResponseEntity.ok(teamMainDto);
    }


    // 팀 생성
    // 인증기능 구현 완료 후 수정할 부분입니다 !!!!!
    // 현재는 @RequestParam 사용 : 쿼리파라미터에 로그인된 사용자의 id를 넣고 사용하면 됩니다
    // ex : /api/teams?userId=3
    @Operation(
            summary = "팀 생성",
            description = "새로운 팀을 생성하고, 팀을 생성한 유저를 해당 팀 팀장으로 등록합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "팀 생성 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    @PostMapping
    public ResponseEntity<?> createTeam(
            @Parameter(description = "로그인된 사용자 ID") @RequestParam("userId") Long userId,
            @RequestBody @Valid TeamCreateRequest request)
    {
        Long teamId = teamService.createTeam(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(programo._pro.global.ApiResponse.success(teamId, teamId+"번 팀을 생성함!"));
    }


    // 팀 정보 수정
    @Operation(
            summary = "팀 정보 수정",
            description = "팀장 권한의 멤버가 팀의 정보를 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "팀 정보 수정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 팀"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    @PatchMapping("/{teamId}")
    public ResponseEntity<?> updateTeam(
            @Parameter(description = "수정할 팀 ID") @PathVariable("teamId") Long teamId,
            @RequestBody @Valid TeamCreateRequest request
    ) {
        teamService.updateTeam(teamId, request);
        return ResponseEntity.ok(programo._pro.global.ApiResponse.success(teamId, teamId+"번 팀 정보를 수정함!"));
    }


    // 팀 삭제
    @Operation(
            summary = "팀 삭제",
            description = "팀장 권한의 멤버가 팀을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "팀 삭제 성공"),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 팀"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    @DeleteMapping("/{teamId}")
    public ResponseEntity<?> deleteTeam(
            @Parameter(description = "삭제할 팀 ID") @PathVariable("teamId") Long teamId)
    {
        teamService.deleteTeam(teamId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }



    // 팀원 내보내기
    @Operation(
            summary = "팀원 내보내기",
            description = "팀장 권한의 멤버가 특정 팀원을 팀에서 내보냅니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "팀원 내보내기 성공"),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 팀 또는 유저"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    @DeleteMapping("/{teamId}/members/{userId}")
    public ResponseEntity<?> kickMember(
            @Parameter(description = "팀 ID") @PathVariable("teamId") Long teamId,
            @Parameter(description = "내보낼 유저 ID") @PathVariable("userId") Long userId
    ) {
        teamService.kickMember(teamId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }



    // 팀 가입
    // 마찬가지로 인증 구현 완료 후 수정할 부분 22
    @Operation(
            summary = "팀 가입",
            description = "사용자가 특정 팀에 팀원으로 가입합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "팀 가입 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    @PostMapping("/{teamId}/members")
    public ResponseEntity<?> joinTeam(
            @Parameter(description = "가입할 팀 ID") @PathVariable("teamId") Long teamId,
            @Parameter(description = "현재 로그인된 사용자 ID") @RequestParam("userId") Long userId
    ) {
        teamService.joinTeam(teamId, userId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(programo._pro.global.ApiResponse.success(teamId, teamId+"번 팀에 가입함!"));
    }



    // 팀 탈퇴
    // 마찬가지로 인증 구현 완료 후 수정할 부분 333
    @Operation(
            summary = "팀 탈퇴",
            description = "팀원 권한의 멤버가 팀에서 탈퇴합니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "팀 탈퇴 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 팀 또는 유저"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    @DeleteMapping("/{teamId}/members")
    public ResponseEntity<?> leaveTeam(
            @Parameter(description = "탈퇴할 팀 ID") @PathVariable("teamId") Long teamId,
            @Parameter(description = "현재 로그인된 사용자 ID") @RequestParam("userId") Long userId
    ) {
        teamService.leaveTeam(teamId, userId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
