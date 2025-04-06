package BallFan.dto.ticket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HomeResponseDTO {

    private Integer winningStreak;
    private List<TicketPreviewDTO> tickets;

}
