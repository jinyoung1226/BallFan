package BallFan.service;

import BallFan.authentication.UserDetailsServiceImpl;
import BallFan.dto.user.UpdateNicknameDTO;
import BallFan.dto.user.UpdateTeamDTO;
import BallFan.dto.user.UserDTO;
import BallFan.entity.user.User;
import BallFan.s3.S3Uploader;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {

    @Value("${cloud.aws.s3.bucket}")
    private String DirName;
    private final UserDetailsServiceImpl userDetailsService;
    private final S3Uploader s3Uploader;

    /**
     * 회원 정보 조회 메서드
     * @return 조회한 UserDTO 반환
     */
    public UserDTO getUser() {
        User user = userDetailsService.getUserByContextHolder();
        return UserDTO.from(user);
    }

    /**
     * 회원 닉네임 수정 메서드
     * @param updateNicknameDTO
     */
    @Transactional
    public void updateNickname(UpdateNicknameDTO updateNicknameDTO) {
        User user = userDetailsService.getUserByContextHolder();
        user.updateNickname(updateNicknameDTO.getNickname());
    }

    /**
     * 회원 팀 수정 메서드
     * @param updateTeamDTO
     */
    @Transactional
    public void updateTeam(UpdateTeamDTO updateTeamDTO) {
        User user = userDetailsService.getUserByContextHolder();
        user.updateTeam(updateTeamDTO.getTeam());
    }

    /**
     * 회원 프로필 이미지 수정 메서드
     * @param imageFile
     */
    @Transactional
    public void updateImage(MultipartFile imageFile) {
        User user = userDetailsService.getUserByContextHolder();

        // 프로필 이미지 변경
        String imagePath = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imagePath = s3Uploader.uploadImage(imageFile, DirName);
            user.updateImage(imagePath);
        } else {
            user.updateImage(null);
        }
    }
}
