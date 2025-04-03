package BallFan.repository;

import BallFan.entity.StadiumVisit;
import BallFan.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StadiumVisitRepository extends JpaRepository<StadiumVisit, Integer> {

    Optional<StadiumVisit> findByUserIdAndStadium(User user, String stadium);
}
