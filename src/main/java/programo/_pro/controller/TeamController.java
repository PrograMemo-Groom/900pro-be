package programo._pro.controller;

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
import java.util.Map;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    public ResponseEntity<List<TeamCardDto>> getAllTeams(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "level", required = false) String level,
            @RequestParam(value = "sort", required = false) String sort
    ) {
        List<TeamCardDto> teamCards = teamService.getAllTeams(keyword, level, sort);
        return ResponseEntity.ok(teamCards);
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamMainDto> getTeamMain(@PathVariable("teamId") Long teamId) {
        TeamMainDto teamMainDto = teamService.getTeamMain(teamId);
        return ResponseEntity.ok(teamMainDto);
    }

    // 인증기능 구현 완료 후 수정할 부분입니다 !!!!!
    // 현재는 @RequestParam 사용 : 쿼리파라미터에 로그인된 사용자의 id를 넣고 사용하면 됩니다
    // ex : /api/teams?userId=3
    @PostMapping
    public ResponseEntity<?> createTeam(
            @RequestParam("userId") Long userId,
            @RequestBody @Valid TeamCreateRequest request) {
        Long teamId = teamService.createTeam(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "status", HttpStatus.CREATED.value(),
                        "teamId", teamId,
                        "message", "팀 생성 완료"
                ));
    }

    @PatchMapping("/{teamId}")
    public ResponseEntity<?> updateTeam(
            @PathVariable("teamId") Long teamId,
            @RequestBody @Valid TeamCreateRequest request
    ) {
        teamService.updateTeam(teamId, request);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "team ID " + teamId + "번 팀 정보를 수정함",
                "teamId", teamId
        ));
    }

    // 마찬가지로 인증 구현 완료 후 수정할 부분 22
    @PostMapping("/{teamId}/members")
    public ResponseEntity<?> joinTeam(
            @PathVariable("teamId") Long teamId,
            @RequestParam("userId") Long userId
    ) {
        teamService.joinTeam(teamId, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "status", HttpStatus.CREATED.value(),
                        "message", "team ID " + teamId + "번 팀에 가입됨",
                        "teamId", teamId
                )
        );
    }

    // 마찬가지로 인증 구현 완료 후 수정할 부분 333
    @DeleteMapping("/{teamId}/members")
    public ResponseEntity<?> leaveTeam(
            @PathVariable("teamId") Long teamId,
            @RequestParam("userId") Long userId
    ) {
        teamService.leaveTeam(teamId, userId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
