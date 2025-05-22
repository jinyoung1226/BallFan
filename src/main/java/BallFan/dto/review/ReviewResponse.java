package BallFan.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponse {

    private Long id;
    private String seat;
    private String content;
    private String stadium;
    private int likes;
    private boolean liked;
    private LocalDateTime createdAt;
    private List<ReviewPhotoDto> photos;

}
