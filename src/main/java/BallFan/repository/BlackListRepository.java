package BallFan.repository;

import BallFan.entity.token.BlackList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlackListRepository extends JpaRepository<BlackList, Long> {
    BlackList findBlackListByRefreshToken(String refresh_token);
}
