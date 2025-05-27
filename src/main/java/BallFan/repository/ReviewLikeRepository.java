package BallFan.repository;

import BallFan.entity.review.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    Optional<ReviewLike> findByUserIdAndReviewId(Long userId, Long reviewId);

    List<ReviewLike> findByUserId(Long userId);

}
