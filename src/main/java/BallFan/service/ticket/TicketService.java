package BallFan.service.ticket;

import BallFan.authentication.UserDetailsServiceImpl;
import BallFan.entity.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final UserDetailsServiceImpl userDetailsService;
    private final WebClient webClient;

    /**
     * 종이 티켓 이미지를 받아, 종이티켓 OCR 서버로 넘겨주는 메서드
     * @param file
     */
    public void registerPaperTicket(MultipartFile file) {
        User user = userDetailsService.getUserByContextHolder();
        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();

            builder.part("file", new ByteArrayResource(file.getBytes())).contentType(MediaType.APPLICATION_OCTET_STREAM); // 또는 IMAGE_PNG, IMAGE_JPEG

            webClient.post()
                    .uri("/upload_paperTicket")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(response -> System.out.println("응답: " + response));

        } catch (IOException e) {
            throw new RuntimeException("이미지 전송 실패", e);
        }
    }
}
