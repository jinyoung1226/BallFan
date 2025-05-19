package BallFan.dto.review;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewPhotoDto {

    private Long id;
    private String photoUrl;
}
