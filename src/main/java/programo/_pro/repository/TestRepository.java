package programo._pro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import programo._pro.entity.Team;
import programo._pro.entity.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TestRepository extends JpaRepository<Test, Integer> {

    List<Test> findByTeamIdAndCreatedAt(Long teamId, LocalDate createdAt);

    List<Test> team(Team team);

    List<Test> findByTeam_Id(Long teamId);
}
