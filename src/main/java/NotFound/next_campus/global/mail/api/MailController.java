package NotFound.next_campus.global.mail.api;

import NotFound.next_campus.global.common.CommonResponse;
import NotFound.next_campus.global.mail.service.MailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Tag(name = "Mail", description = "메일 발송 관련 API")
@RestController
@RequestMapping("/mail")
public class MailController {

    private final MailService mailService;

    public MailController(MailService mailService) {
        this.mailService = mailService;
    }

    @Operation(summary = "개인 분실물 알림 메일 발송", description = "분실물을 찾은 사용자에게 개인 알림 메일을 발송합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메일 전송 성공"),
            @ApiResponse(responseCode = "500", description = "MAIL001 - 메일 전송에 실패했습니다.")
    })
    @PostMapping("/sendPersonalLost")
    public ResponseEntity<CommonResponse<String>> sendPersonalLostMail(
            @Parameter(description = "수신자 이메일") @RequestParam String to,
            @Parameter(description = "수신자 이름") @RequestParam String name,
            @Parameter(description = "분실물 이름") @RequestParam String title,
            @Parameter(description = "분실물 등록일시 (ISO-8601, 예: 2026-10-02T14:00:00)") @RequestParam String createdAt
    ) {
        // 문자열로 전달된 날짜를 LocalDateTime으로 변환
        LocalDateTime createdDate = LocalDateTime.parse(createdAt, DateTimeFormatter.ISO_DATE_TIME);

        mailService.sendPersonalLostEmail(to, name, title, createdDate);
        return ResponseEntity.ok(CommonResponse.ok("개인 분실물 메일 전송 완료"));
    }
}
