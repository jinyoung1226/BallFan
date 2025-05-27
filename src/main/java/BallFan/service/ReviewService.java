package BallFan.service;

import BallFan.authentication.UserDetailsServiceImpl;
import BallFan.dto.response.BaseResponse;
import BallFan.dto.review.*;
import BallFan.entity.Ticket;
import BallFan.entity.review.Review;
import BallFan.entity.review.ReviewLike;
import BallFan.entity.review.ReviewPhoto;
import BallFan.entity.user.User;
import BallFan.exception.review.ReviewAlreadyExistsException;
import BallFan.exception.review.ReviewNotFoundException;
import BallFan.exception.ticket.TicketNotFoundException;
import BallFan.repository.ReviewLikeRepository;
import BallFan.repository.ReviewPhotoRepository;
import BallFan.repository.ReviewRepository;
import BallFan.repository.TicketRepository;
import BallFan.s3.S3Uploader;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewPhotoRepository reviewPhotoRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    @Value("${cloud.aws.s3.bucket}")
    private String DirName;
    private final WebClient webClient2;
    private static final String TICKET_NOT_FOUND_MESSAGE = "티켓이 존재하지 않습니다";
    private static final String TICKET_ALREADY_EXIST_MESSAGE = "이미 해당 티켓에 리뷰가 존재합니다";
    private static final String REVIEW_NOT_FOUND_MESSAGE = "리뷰가 존재하지 않습니다";
    private final UserDetailsServiceImpl userDetailsService;
    private final ReviewRepository reviewRepository;
    private final TicketRepository ticketRepository;
    private final S3Uploader s3Uploader;

    /**
     * Review entity 만드는 메서드
     * @param content 리뷰 내용
     * @param stadium 리뷰 경기장
     * @param seat 리뷰 좌석
     * @param ticket 리뷰 관련 티켓
     * @param user 리뷰 쓴 유저
     * @return reivew entity
     */
    private Review createReviewEntity(String content, String stadium, String seat, Ticket ticket, User user) {
        return Review.builder()
                .seat(seat)
                .content(content)
                .stadium(stadium)
                .createdAt(LocalDateTime.now())
                .likes(0)
                .ticket(ticket)
                .user(user)
                .build();
    }

    /**
     * 리뷰 사진 등록하는 메서드
     * @param review 리뷰
     * @param photos 리뷰 사진
     */
    private void attachReviewPhotos(Review review, List<MultipartFile> photos) {
        if (photos == null || photos.isEmpty()) return;

        List<ReviewPhoto> photoList = photos.stream()
                .filter(photo -> !photo.isEmpty())
                .map(photo -> ReviewPhoto.builder()
                        .photoUrl(s3Uploader.uploadImage(photo, DirName))
                        .review(review)
                        .build())
                .collect(Collectors.toList());

        review.getPhotos().addAll(photoList);
    }

    /**
     * 리뷰 좌석 벡터 DB 저장하는 메서드
     * @param review 리뷰 엔티티
     */
    private void saveSeatVectorDB(Review review) {
        SeatReviewRequest seatReviewRequest = new SeatReviewRequest(
                review.getId(),
                review.getSeat(),
                review.getStadium()
        );

        String response = webClient2.post()
                .uri("/review/save/seat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(seatReviewRequest)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println(response);
    }

    /**
     * 리뷰 텍스트 벡터 DB 저장하는 메서드
     * @param review 리뷰 엔티티
     */
    private void saveTextVectorDB(Review review) {
        TextReviewReqeust textReviewReqeust = new TextReviewReqeust(
                review.getId(),
                review.getContent(),
                review.getStadium()
        );

        String response = webClient2.post()
                .uri("/review/save/text")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(textReviewReqeust)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println(response);
    }

    /**
     * 리뷰 삭제 시, 좌석과 텍스트 벡터 DB 값도 삭제하는 메서드
     * @param reviewId 리뷰 ID
     */
    private void deleteSeatAndTextVectorDB(Long reviewId) {
        String url = "/review/delete/" + reviewId;

        String response = webClient2.delete()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println(response);
    }

    /**
     * 리뷰 유사 좌석 조회하는 메서드
     * @param review 리뷰 엔티티
     */
    private List<Long> getSimilarSeatReviewIds(Review review) {
        SeatReviewRequest seatReviewRequest = new SeatReviewRequest(
                review.getId(),
                review.getSeat(),
                review.getStadium()
        );

        List<Long> similarSeatIds = webClient2.post()
                .uri("/review/get/seat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(seatReviewRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, List<Long>>>() {})
                .block()
                .get("review_ids");

        System.out.println(similarSeatIds);

        return similarSeatIds;
    }

    /**
     * 리뷰 유사 텍스트 조회하는 메서드
     * @param review 리뷰 엔티티
     */
    private List<Long> getSimilarTextReviewIds(Review review) {
        TextReviewReqeust textReviewReqeust = new TextReviewReqeust(
                review.getId(),
                review.getContent(),
                review.getStadium()
        );

        List<Long> similarTextIds = webClient2.post()
                .uri("/review/get/text")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(textReviewReqeust)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, List<Long>>>() {})
                .block()
                .get("review_ids");

        System.out.println(similarTextIds);

        return similarTextIds;
    }

    /**
     * 리뷰에 관한 키워드 요약 조회하는 메서드
     * @param review 리뷰 엔티티
     */
    private List<String> getSimilarKeyword(Review review) {
        TextReviewReqeust textReviewReqeust = new TextReviewReqeust(
                review.getId(),
                review.getContent(),
                review.getStadium()
        );

        List<String> similarKeyword = webClient2.post()
                .uri("/review/get/summary")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(textReviewReqeust)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, List<String>>>() {})
                .block()
                .get("summary");

        System.out.println(similarKeyword);

        return similarKeyword;
    }

    /**
     * 해당 리뷰에 관한 장소 정보 조히
     * @param review 리뷰 엔티티
     */
    private List<PlaceInfo> getPlaceInfo(Review review) {
        TextReviewReqeust textReviewReqeust = new TextReviewReqeust(
                review.getId(),
                review.getContent(),
                review.getStadium()
        );

        List<PlaceInfo> placeInfo = webClient2.post()
                .uri("/review/get/place")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(textReviewReqeust)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, List<PlaceInfo>>>() {})
                .block()
                .get("places");

        System.out.println(placeInfo);

        return placeInfo;
    }

    /**
     * 리뷰 객체 생성하는 메서드
     * @param review 리뷰
     * @return 리뷰 객체
     */
    private SimpleReviewDTO toSimpleReviewDto(Review review, User user) {
        String photoUrl = null;
        if (review.getPhotos() != null && !review.getPhotos().isEmpty()) {
            photoUrl = review.getPhotos().get(0).getPhotoUrl();
        }

        // 리뷰 정보 조회
        List<ReviewLike> likeList = review.getLikeList();
        boolean liked = false;
        for (ReviewLike reviewLike : likeList) {
            if(reviewLike.getUser().getId().equals(user.getId())) {
                liked = true;
            }
        }

        return SimpleReviewDTO.builder()
                .id(review.getId())
                .seat(review.getSeat())
                .photoUrl(photoUrl)
                .likes(review.getLikes())
                .liked(liked)
                .build();
    }

    private AllReviewResponse convertToDTO(Review review, User user) {
        List<ReviewPhotoDto> photos = review.getPhotos() != null
                ? review.getPhotos().stream()
                .map(photo -> ReviewPhotoDto.builder()
                        .id(photo.getId())
                        .photoUrl(photo.getPhotoUrl())
                        .build())
                .collect(Collectors.toList())
                : Collections.emptyList();

        List<ReviewLike> likeList = review.getLikeList();
        boolean liked = false;
        for (ReviewLike reviewLike : likeList) {
            if(reviewLike.getUser().getId().equals(user.getId())) {
                liked = true;
            }
        }

        return AllReviewResponse.builder()
                .id(review.getId())
                .nickname(review.getUser().getNickname())
                .image(review.getUser().getImage())
                .seat(review.getSeat())
                .content(review.getContent())
                .stadium(review.getStadium())
                .likes(review.getLikes())
                .liked(liked)
                .createdAt(review.getCreatedAt())
                .photos(photos)
                .build();
    }

    /**
     * 리뷰 등록하는 메서드
     * @param content 리뷰 텍스트
     * @param stadium 리뷰 경기장
     * @param seat 리뷰 좌석
     * @param ticketId 리뷰를 쓰는 티켓 ID
     * @param photos 리뷰 사진
     */
    @Transactional
    public BaseResponse createReview(String content, String stadium, String seat, Long ticketId, List<MultipartFile> photos) {
        User user = userDetailsService.getUserByContextHolder();

        // 해당 티켓에 대한 리뷰가 존재하는지 확인
        if (reviewRepository.existsByTicketId(ticketId)) {
            throw new ReviewAlreadyExistsException(TICKET_ALREADY_EXIST_MESSAGE);
        }

        // 티켓 존재하는지 확인
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(TICKET_NOT_FOUND_MESSAGE));

        // 리뷰 엔티티 생성
        Review review = createReviewEntity(content, stadium, seat, ticket, user);

        // 해당 티켓에 리뷰 id 삽입
        ticket.updateReview(review);

        // 리뷰 사진 등록
        attachReviewPhotos(review, photos);

        // 리뷰 저장
        reviewRepository.save(review);

        // 리뷰 등록 시, 좌석 정보 벡터 DB 저장
        saveSeatVectorDB(review);

        // 리뷰 등록 시, 텍스트 정보 벡터 DB 저장
        saveTextVectorDB(review);

        return new BaseResponse("리뷰 등록이 완료되었습니다");
    }

    /**
     * 리뷰 삭제하는 메서드
     * @param reviewId 리뷰 ID
     * @return 성공 메시지
     */
    @Transactional
    public BaseResponse deleteReview(Long reviewId) {
        User user = userDetailsService.getUserByContextHolder();

        // 리뷰 존재하는지 확인
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(REVIEW_NOT_FOUND_MESSAGE));

        // 해당 티켓에서 리뷰 id 삭제 먼저 조치
        Ticket ticket = review.getTicket();
        ticket.updateReview(null);

        // 해당 리뷰 삭제
        reviewRepository.delete(review);

        // 해당 리뷰의 사진 삭제
        List<ReviewPhoto> photos = review.getPhotos();
        for (ReviewPhoto photo : photos) {
            reviewPhotoRepository.delete(photo);
        }

        // 리뷰 좌석, 텍스트 벡터 DB도 삭제
        deleteSeatAndTextVectorDB(review.getId());

        return new BaseResponse("리뷰 삭제가 완료되었습니다");

    }

    /**
     * 리뷰 조회하는 메서드
     * @param reviewId 조회하는 리뷰 ID
     */
    public TotalReviewResponse getReview(Long reviewId) {
        User user = userDetailsService.getUserByContextHolder();

        // 리뷰 존재하는지 확인
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(REVIEW_NOT_FOUND_MESSAGE));

        // 유사 좌석 리뷰 객체 조회 및 생성
        List<Long> similarSeatReviewIds = getSimilarSeatReviewIds(review);
        List<SimpleReviewDTO> similarSeatReviews = similarSeatReviewIds.stream()
                .map(id -> reviewRepository.findById(id)
                        .map(r -> toSimpleReviewDto(r, user))
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 유사 텍스트 리뷰 객체 조회 및 생성
        List<Long> similarTextReviewIds = getSimilarTextReviewIds(review);
        List<SimpleReviewDTO> similarTextReviews = similarTextReviewIds.stream()
                .map(id -> reviewRepository.findById(id)
                        .map(r -> toSimpleReviewDto(r, user))
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());


        // 유사 키워드 조회
        List<String> similarKeyword = getSimilarKeyword(review);

        // 해당 리뷰의 장소 정보 조회
        List<PlaceInfo> placeInfo = getPlaceInfo(review);

        List<ReviewPhotoDto> photoDtos = review.getPhotos().stream()
                .map(photo -> ReviewPhotoDto.builder()
                        .id(photo.getId())
                        .photoUrl(photo.getPhotoUrl())
                        .build())
                .collect(Collectors.toList());

        // 리뷰 정보 조회
        List<ReviewLike> likeList = review.getLikeList();
        boolean liked = false;
        for (ReviewLike reviewLike : likeList) {
            if(reviewLike.getUser().getId().equals(user.getId())) {
                liked = true;
            }
        }

        ReviewResponse reviewResponse = ReviewResponse.builder()
                .id(review.getId())
                .nickname(review.getUser().getNickname())
                .image(review.getUser().getImage())
                .seat(review.getSeat())
                .content(review.getContent())
                .stadium(review.getStadium())
                .createdAt(review.getCreatedAt())
                .likes(review.getLikes())
                .liked(liked)
                .photos(photoDtos)
                .build();

        // 모든 리뷰 정보 조회 반환
        return TotalReviewResponse.builder()
                .reviewResponse(reviewResponse)
                .similarSeatReviews(similarSeatReviews)
                .similarTextReviews(similarTextReviews)
                .summaryKeywords(similarKeyword)
                .places(placeInfo)
                .build();
    }

    /**
     * 전체 리뷰 조회하는 메서드
     * @param pageable
     * @return 리뷰 전체 반환
     */
    public Page<AllReviewResponse> getAllReview(Pageable pageable) {
        User user = userDetailsService.getUserByContextHolder();

        Page<Review> reviewPage = reviewRepository.findAllByOrderByCreatedAtDesc(pageable);

        return reviewPage.isEmpty()
                ? Page.empty()
                : reviewPage.map(review -> convertToDTO(review, user));
    }

    /**
     * 리뷰에 좋아요 하는 메서드
     * @param reviewId 리뷰 ID
     */
    public void pressLike(Long reviewId) {
        User user = userDetailsService.getUserByContextHolder();

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(REVIEW_NOT_FOUND_MESSAGE));

        Optional<ReviewLike> existingLike = reviewLikeRepository.findByUserIdAndReviewId(user.getId(), review.getId());
        int likesCount;

        if(existingLike.isPresent()) {
            reviewLikeRepository.delete(existingLike.get());
            likesCount = review.getLikes() - 1;

            review.updateLikes(likesCount);
            reviewRepository.save(review);

        } else {
            ReviewLike newLike = ReviewLike.builder()
                    .user(user)
                    .review(review)
                    .createdAt(LocalDate.now())
                    .build();
            reviewLikeRepository.save(newLike);

            likesCount = review.getLikes() + 1;

            review.updateLikes(likesCount);
            reviewRepository.save(review);

        }
    }

    public List<MyReviewResponse> getMyReview() {
        User user = userDetailsService.getUserByContextHolder();

        List<Review> myReview = reviewRepository.findByUserId(user.getId());
        
        List<MyReviewResponse> myReviewResponse = new ArrayList<>();;

        for (Review review : myReview) {
            MyReviewResponse review1 = MyReviewResponse.builder()
                    .id(review.getId())
                    .createdAt(review.getCreatedAt().toLocalDate())
                    .dayOfWeek(review.getCreatedAt().getDayOfWeek().toString())
                    .stadium(review.getStadium())
                    .image(review.getPhotos().get(0).getPhotoUrl())
                    .build();
            myReviewResponse.add(review1);
        }
        return myReviewResponse;
    }

    public List<MyReviewLikesResponse> getMyLikes() {
        User user = userDetailsService.getUserByContextHolder();

        List<ReviewLike> reviewLikes = reviewLikeRepository.findByUserId(user.getId());
        List<MyReviewLikesResponse> myReviewLikesResponse = new ArrayList<>();

        for (ReviewLike reviewLike : reviewLikes) {
            MyReviewLikesResponse build = MyReviewLikesResponse.builder()
                    .id(reviewLike.getReview().getId())
                    .createdAt(reviewLike.getReview().getCreatedAt().toLocalDate())
                    .dayOfWeek(reviewLike.getReview().getCreatedAt().getDayOfWeek().toString())
                    .stadium(reviewLike.getReview().getStadium())
                    .image(reviewLike.getReview().getPhotos().get(0).getPhotoUrl())
                    .liked(true)
                    .build();
            myReviewLikesResponse.add(build);
        }
        return myReviewLikesResponse;
    }
}
