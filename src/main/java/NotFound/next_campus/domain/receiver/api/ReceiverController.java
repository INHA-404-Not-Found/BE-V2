package NotFound.next_campus.domain.receiver.api;

import NotFound.next_campus.domain.receiver.dto.ReceiverDTO;
import NotFound.next_campus.domain.receiver.service.ReceiverService;
import NotFound.next_campus.global.auth.user.CustomUserDetails;
import NotFound.next_campus.global.common.CommonResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "수령인 (관리자)", description = "수령인 API (수령인 등록 · 수정 · 삭제 · 조회)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/receivers")
public class ReceiverController {

    private final ReceiverService receiverService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<Long> registerReceiver(
            @RequestBody ReceiverDTO.CreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return CommonResponse.ok(
                receiverService.saveReceiver(request, userDetails)
        );
    }

    @PatchMapping("/{receiver_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public CommonResponse<Void> updateReceiver(
            @PathVariable("receiver_id") Long receiverId,
            @RequestBody ReceiverDTO.UpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        receiverService.updateReceiver(receiverId, request, userDetails);

        return CommonResponse.ok();
    }

    @DeleteMapping("/{receiver_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public CommonResponse<Void> deleteReceiver(
            @PathVariable("receiver_id") Long receiverId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        receiverService.deleteReceiver(receiverId, userDetails);

        return CommonResponse.ok();
    }

    /* 특정 수령인 조회 */
    @GetMapping("/{receiver_id}")
    public CommonResponse<ReceiverDTO.Response> getReceiver(
            @PathVariable("receiver_id") Long receiverId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return CommonResponse.ok(
                receiverService.getReceiverInfo(receiverId, userDetails)
        );
    }

    /* 특정 게시물의 수령인 조회 */
    @GetMapping("/posts/{post_id}")
    public CommonResponse<ReceiverDTO.Response> getReceiverByPost(
            @PathVariable("post_id") Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return CommonResponse.ok(
                receiverService.getReceiverByPost(postId, userDetails)
        );
    }

    /* 모든 수령인 목록 조회 */
    @GetMapping
    public CommonResponse<List<ReceiverDTO.Response>> getAllReceivers(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return CommonResponse.ok(
                receiverService.getAllReceivers(userDetails)
        );
    }
}
