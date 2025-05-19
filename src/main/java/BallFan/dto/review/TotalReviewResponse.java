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
    private List<Long> similarSeatReviewIds;
    private List<Long> similarTextReviewIds;
    private List<String> summaryKeywords;
    private List<PlaceInfo> places;

}
