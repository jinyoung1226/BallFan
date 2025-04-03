package BallFan.repository;

import BallFan.entity.Team;
import BallFan.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {

    Optional<Ticket> findByUserIdAndTicketDateAndHomeTeamAndAwayTeam(Long userId, LocalDate ticketDate, Team homeTeam, Team awayTeam);
}
