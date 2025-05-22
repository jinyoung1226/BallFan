package BallFan.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TotalReviewResponse {

    private ReviewResponse reviewResponse;
    private List<SimpleReviewDTO> similarSeatReviews;
    private List<SimpleReviewDTO> similarTextReviews;
    private List<String> summaryKeywords;
    private List<PlaceInfo> places;

}
