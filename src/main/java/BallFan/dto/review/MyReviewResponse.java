package BallFan.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MyReviewResponse {

    private Long id;
    private String image;
    private String stadium;
    private LocalDate createdAt;
}
