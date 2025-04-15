package programo._pro.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import programo._pro.dto.TeamCardDto;
import programo._pro.service.TeamService;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    public ResponseEntity<List<TeamCardDto>> getAllTeams() {
        List<TeamCardDto> teamCards = teamService.getAllTeams();
        return ResponseEntity.ok(teamCards);
    }
}
