package NotFound.next_campus.domain.notification.api;

import NotFound.next_campus.domain.notification.dto.NotificationDTO;
import NotFound.next_campus.domain.notification.service.NotificationService;
import NotFound.next_campus.global.auth.user.CustomUserDetails;
import NotFound.next_campus.global.common.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    // 특정 유저에게 알림 발송
    /*@PostMapping
    public ResponseEntity<String> sendNotification(
            @RequestBody NotificationDTO.CreateRequest request
    ) {
        notificationService.sendAndSaveNotification(request);

        return ResponseEntity.ok().body(
                "알림 저장 및 발송 성공"
        );
    }*/

    @GetMapping
    public CommonResponse<List<NotificationDTO.Response>> getMyNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(page = 1) Pageable pageable,
            @RequestParam(required = false, defaultValue = "0", value = "page") int pageNo
    ) {
        pageNo = (pageNo == 0) ? 0 : pageNo - 1;

        return CommonResponse.ok(
                notificationService.getNotifications(userDetails, pageable, pageNo)
        );
    }

    @PatchMapping("/{notification_id}/read")
    public CommonResponse<String> readNotification(
            @PathVariable("notification_id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        notificationService.markAsRead(id, userDetails);

        return CommonResponse.ok(
                "알림 읽기 성공"
        );
    }
}
