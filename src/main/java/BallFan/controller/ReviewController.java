package BallFan.controller;

import BallFan.dto.response.BaseResponse;
import BallFan.dto.review.TotalReviewResponse;
import BallFan.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/{ticketId}")
    public ResponseEntity<BaseResponse> createReview(@PathVariable Long ticketId,
                                                     @RequestParam(name = "content") String content,
                                                     @RequestParam(name = "stadium") String stadium,
                                                     @RequestParam(name = "seat") String seat,
                                                     @RequestPart(value = "photos", required = false) List<MultipartFile> photos) {
        BaseResponse baseResponse = reviewService.createReview(content, stadium, seat, ticketId, photos);
        return ResponseEntity.ok(baseResponse);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<BaseResponse> deleteReview(@PathVariable Long reviewId) {
        BaseResponse baseResponse = reviewService.deleteReview(reviewId);
        return ResponseEntity.ok(baseResponse);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<TotalReviewResponse> getReview(@PathVariable Long reviewId) {
        TotalReviewResponse totalReviewResponse = reviewService.getReview(reviewId);
        return ResponseEntity.ok(totalReviewResponse);
    }
}
