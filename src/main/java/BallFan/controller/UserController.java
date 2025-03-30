package BallFan.controller;

import BallFan.dto.user.UpdateNicknameDTO;
import BallFan.dto.user.UpdateTeamDTO;
import BallFan.dto.user.UserDTO;
import BallFan.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원 정보 조회
    @GetMapping
    public ResponseEntity<UserDTO> getUser() {
        UserDTO user = userService.getUser();
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/nickname")
    public ResponseEntity<Void> updateNickname(@RequestBody UpdateNicknameDTO updateNicknameDTO) {
        userService.updateNickname(updateNicknameDTO);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/team")
    public ResponseEntity<Void> updateTeam(@RequestBody UpdateTeamDTO updateTeamDTO) {
        userService.updateTeam(updateTeamDTO);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/image")
    public ResponseEntity<Void> updateImage(@RequestPart MultipartFile file) {
        userService.updateImage(file);
        return ResponseEntity.ok().build();
    }
}
