package BallFan.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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
    private LocalDate createdAt;
    private List<ReviewPhotoDto> photos;

}
