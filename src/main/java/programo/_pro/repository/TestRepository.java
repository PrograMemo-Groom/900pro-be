package programo._pro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import programo._pro.entity.Test;

public interface TestRepository extends JpaRepository<Test, Integer> {

}
