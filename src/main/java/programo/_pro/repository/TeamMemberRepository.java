package programo._pro.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import programo._pro.entity.TeamMember;
import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    List<TeamMember> findByUserId(Long userId);
    // teamId로 해당 팀 팀원들만 조회
    List<TeamMember> findByTeam_Id(Long teamId);

    boolean existsByUserId(Long userId);

    Optional<TeamMember> findByTeamIdAndUserId(Long teamId, Long userId);

    @Modifying(clearAutomatically = true)
    void deleteByTeam_Id(Long teamId);
}
