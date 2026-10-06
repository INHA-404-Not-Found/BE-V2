package NotFound.next_campus.domain.receiver.api;

import NotFound.next_campus.domain.receiver.dto.ReceiverDTO;
import NotFound.next_campus.domain.receiver.service.ReceiverService;
import NotFound.next_campus.global.auth.user.CustomUserDetails;
import NotFound.next_campus.global.common.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "수령인 (관리자)", description = "수령인 API (수령인 등록 · 수정 · 삭제 · 조회). 모든 API에 ADMIN 권한이 필요합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/receivers")
public class ReceiverController {

    private final ReceiverService receiverService;

    /* 수령인 등록 */
    @Operation(summary = "수령인 등록", description = "게시물의 분실물을 수령한 사람의 정보(이름 · 이메일 · 전화번호 · 학번)를 등록하고 생성된 수령인 ID를 반환합니다. ADMIN 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "수령인 등록 성공 (data: 수령인 ID)"),
            @ApiResponse(responseCode = "401", description = "AUTH001 - 인증 토큰이 없거나 유효하지 않습니다."),
            @ApiResponse(responseCode = "403", description = "RECEIVER002 - 수령인 등록 권한이 없습니다."),
            @ApiResponse(responseCode = "404", description = "POST001 - 존재하지 않는 게시물입니다.")
    })
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

    /* 수령인 수정 */
    @Operation(summary = "수령인 수정", description = "수령인 정보를 수정합니다. ADMIN 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수령인 수정 성공 (응답 body 없음)"),
            @ApiResponse(responseCode = "401", description = "AUTH001 - 인증 토큰이 없거나 유효하지 않습니다."),
            @ApiResponse(responseCode = "403", description = "RECEIVER003 - 수령인 수정 권한이 없습니다."),
            @ApiResponse(responseCode = "404", description = "RECEIVER001 - 존재하지 않는 수령인입니다.")
    })
    @PatchMapping("/{receiver_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateReceiver(
            @Parameter(description = "수령인 ID") @PathVariable("receiver_id") Long receiverId,
            @RequestBody ReceiverDTO.UpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        receiverService.updateReceiver(receiverId, request, userDetails);
    }

    /* 수령인 삭제 */
    @Operation(summary = "수령인 삭제", description = "수령인 정보를 삭제합니다. ADMIN 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수령인 삭제 성공 (응답 body 없음)"),
            @ApiResponse(responseCode = "401", description = "AUTH001 - 인증 토큰이 없거나 유효하지 않습니다."),
            @ApiResponse(responseCode = "403", description = "RECEIVER004 - 수령인 삭제 권한이 없습니다."),
            @ApiResponse(responseCode = "404", description = "RECEIVER001 - 존재하지 않는 수령인입니다.")
    })
    @DeleteMapping("/{receiver_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReceiver(
            @Parameter(description = "수령인 ID") @PathVariable("receiver_id") Long receiverId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        receiverService.deleteReceiver(receiverId, userDetails);
    }

    /* 특정 수령인 조회 */
    @Operation(summary = "수령인 단건 조회", description = "수령인 ID로 수령인 정보를 조회합니다. ADMIN 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "AUTH001 - 인증 토큰이 없거나 유효하지 않습니다."),
            @ApiResponse(responseCode = "403", description = "RECEIVER005 - 수령인 조회 권한이 없습니다."),
            @ApiResponse(responseCode = "404", description = "RECEIVER001 - 존재하지 않는 수령인입니다.")
    })
    @GetMapping("/{receiver_id}")
    public CommonResponse<ReceiverDTO.Response> getReceiver(
            @Parameter(description = "수령인 ID") @PathVariable("receiver_id") Long receiverId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return CommonResponse.ok(
                receiverService.getReceiverInfo(receiverId, userDetails)
        );
    }

    /* 특정 게시물의 수령인 조회 */
    @Operation(summary = "게시물별 수령인 조회", description = "게시물 ID로 해당 게시물의 수령인 정보를 조회합니다. "
            + "아직 수령인이 등록되지 않은 게시물이면 404가 아니라 모든 필드가 비어 있는 객체를 반환합니다. ADMIN 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공 (수령인이 없으면 빈 객체)"),
            @ApiResponse(responseCode = "401", description = "AUTH001 - 인증 토큰이 없거나 유효하지 않습니다."),
            @ApiResponse(responseCode = "403", description = "RECEIVER005 - 수령인 조회 권한이 없습니다."),
            @ApiResponse(responseCode = "404", description = "POST001 - 존재하지 않는 게시물입니다.")
    })
    @GetMapping("/posts/{post_id}")
    public CommonResponse<ReceiverDTO.Response> getReceiverByPost(
            @Parameter(description = "게시물 ID") @PathVariable("post_id") Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return CommonResponse.ok(
                receiverService.getReceiverByPost(postId, userDetails)
        );
    }

    /* 모든 수령인 목록 조회 */
    @Operation(summary = "수령인 전체 조회", description = "등록된 모든 수령인 목록을 조회합니다. ADMIN 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "AUTH001 - 인증 토큰이 없거나 유효하지 않습니다."),
            @ApiResponse(responseCode = "403", description = "RECEIVER005 - 수령인 조회 권한이 없습니다.")
    })
    @GetMapping
    public CommonResponse<List<ReceiverDTO.Response>> getAllReceivers(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return CommonResponse.ok(
                receiverService.getAllReceivers(userDetails)
        );
    }
}
