package BallFan.repository;

import BallFan.entity.Team;
import BallFan.entity.Ticket;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByUserIdAndTicketDateAndHomeTeamAndAwayTeam(Long userId, LocalDate ticketDate, Team homeTeam, Team awayTeam);

    List<Ticket> findByUserId(Long userId);

    @Query("SELECT t FROM Ticket t " +
            "WHERE t.user.id = :userId " +
            "AND (t.homeTeam = :team OR t.awayTeam = :team) ")
    List<Ticket> findByUserIdAndFavoriteTeam(
            @Param("userId") Long userId,
            @Param("team") Team team);
}
