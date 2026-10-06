package NotFound.next_campus.domain.notification.api;

import NotFound.next_campus.domain.notification.dto.NotificationDTO;
import NotFound.next_campus.domain.notification.service.NotificationService;
import NotFound.next_campus.global.auth.user.CustomUserDetails;
import NotFound.next_campus.global.common.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "알림", description = "알림 API (내 알림 조회 · 읽음 처리). 모든 API에 로그인이 필요합니다.")
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

    @Operation(summary = "내 알림 조회", description = "로그인한 사용자가 받은 알림을 최신순으로 페이지당 10개씩 조회합니다. "
            + "습득 게시물 등록 시 같은 카테고리의 분실 게시물 작성자에게 생성되는 알림 등이 포함됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "AUTH001 - 인증 토큰이 없거나 유효하지 않습니다.")
    })
    @GetMapping
    public CommonResponse<List<NotificationDTO.Response>> getMyNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(hidden = true) @PageableDefault(page = 1) Pageable pageable,
            @Parameter(description = "페이지 번호 (1부터 시작, 생략 시 1)")
            @RequestParam(required = false, defaultValue = "0", value = "page") int pageNo
    ) {
        pageNo = (pageNo == 0) ? 0 : pageNo - 1;

        return CommonResponse.ok(
                notificationService.getNotifications(userDetails, pageable, pageNo)
        );
    }

    @Operation(summary = "알림 읽음 처리", description = "알림을 읽음 상태로 변경합니다. 알림을 받은 본인만 가능합니다 (ADMIN도 타인의 알림은 처리할 수 없습니다).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "알림 읽음 처리 성공"),
            @ApiResponse(responseCode = "401", description = "AUTH001 - 인증 토큰이 없거나 유효하지 않습니다."),
            @ApiResponse(responseCode = "403", description = "NOTIFICATION002 - 해당 알림에 대한 읽기 권한이 없습니다."),
            @ApiResponse(responseCode = "404", description = "NOTIFICATION001 - 존재하지 않는 알림입니다.")
    })
    @PatchMapping("/{notification_id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void readNotification(
            @Parameter(description = "알림 ID") @PathVariable("notification_id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        notificationService.markAsRead(id, userDetails);
    }
}
