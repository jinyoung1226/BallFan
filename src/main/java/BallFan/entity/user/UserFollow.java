package BallFan.entity.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_follow")
public class UserFollow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 팔로우 누른 사람
    @ManyToOne
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    // 팔로우 당한 사람
    @ManyToOne
    @JoinColumn(name = "following_id", nullable = false)
    private User following;

    private LocalDate createdAt = LocalDate.now();
}
