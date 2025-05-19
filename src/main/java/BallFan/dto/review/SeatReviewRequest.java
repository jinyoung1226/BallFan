package BallFan.dto.review;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatReviewRequest {
    @JsonProperty("review_id")
    private Long review_id;
    private String seat;
    private String stadium;
}
