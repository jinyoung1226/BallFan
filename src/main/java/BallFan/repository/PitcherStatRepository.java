package BallFan.repository;

import BallFan.entity.pitcher.PitcherStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PitcherStatRepository extends JpaRepository<PitcherStat, Long> {
}
