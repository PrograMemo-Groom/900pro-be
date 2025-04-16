package programo._pro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import programo._pro.entity.Team;
import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByIsActiveTrue();
}