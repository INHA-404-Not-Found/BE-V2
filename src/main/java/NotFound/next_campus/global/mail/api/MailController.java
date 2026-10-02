package NotFound.next_campus.global.mail.api;

import NotFound.next_campus.global.common.CommonResponse;
import NotFound.next_campus.global.mail.service.MailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/mail")
public class MailController {

    private final MailService mailService;

    public MailController(MailService mailService) {
        this.mailService = mailService;
    }

    @PostMapping("/sendPersonalLost")
    public ResponseEntity<CommonResponse<String>> sendPersonalLostMail(@RequestParam String to,
                                       @RequestParam String name,
                                       @RequestParam String title,
                                       @RequestParam String createdAt) {
        // 문자열로 전달된 날짜를 LocalDateTime으로 변환
        LocalDateTime createdDate = LocalDateTime.parse(createdAt, DateTimeFormatter.ISO_DATE_TIME);

        mailService.sendPersonalLostEmail(to, name, title, createdDate);
        return ResponseEntity.ok(CommonResponse.ok("개인 분실물 메일 전송 완료"));
    }
}
