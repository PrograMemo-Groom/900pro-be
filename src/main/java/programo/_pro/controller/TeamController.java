package programo._pro.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import programo._pro.dto.TeamCardDto;
import programo._pro.dto.TeamMainDto;
import programo._pro.service.TeamService;

import java.util.List;

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
}
