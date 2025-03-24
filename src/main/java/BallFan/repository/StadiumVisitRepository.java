package BallFan.repository;

import BallFan.entity.StadiumVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StadiumVisitRepository extends JpaRepository<StadiumVisit, Integer> {
}
