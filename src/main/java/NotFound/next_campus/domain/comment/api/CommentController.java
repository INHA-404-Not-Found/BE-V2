package NotFound.next_campus.domain.comment.api;

import NotFound.next_campus.domain.comment.dto.CommentDTO;
import NotFound.next_campus.domain.comment.service.CommentService;
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

@Tag(name = "댓글", description = "댓글 API (댓글 등록 · 수정 · 삭제 · 조회)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 등록", description = "게시물에 댓글을 등록하고 생성된 댓글 ID를 반환합니다. 로그인이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "댓글 등록 성공 (data: 댓글 ID)"),
            @ApiResponse(responseCode = "401", description = "AUTH001 - 인증 토큰이 없거나 유효하지 않습니다."),
            @ApiResponse(responseCode = "404", description = "POST001 - 존재하지 않는 게시물입니다.")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<Long> registerComment(
            @RequestBody CommentDTO.CreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return CommonResponse.ok(
                commentService.saveComment(request, userDetails)
        );
    }

    /* 댓글 수정 */
    @Operation(summary = "댓글 수정", description = "댓글 내용을 수정합니다. 댓글 작성자 또는 ADMIN만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "댓글 수정 성공 (응답 body 없음)"),
            @ApiResponse(responseCode = "401", description = "AUTH001 - 인증 토큰이 없거나 유효하지 않습니다."),
            @ApiResponse(responseCode = "403", description = "COMMENT002 - 해당 댓글에 대한 수정 권한이 없습니다."),
            @ApiResponse(responseCode = "404", description = "COMMENT001 - 존재하지 않는 댓글입니다.")
    })
    @PatchMapping("/{comment_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateComment(
            @Parameter(description = "댓글 ID") @PathVariable("comment_id") Long commentId,
            @RequestBody CommentDTO.UpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        commentService.updateComment(commentId, request, userDetails);
    }

    /* 댓글 삭제 */
    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다. 댓글 작성자 또는 ADMIN만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "댓글 삭제 성공 (응답 body 없음)"),
            @ApiResponse(responseCode = "401", description = "AUTH001 - 인증 토큰이 없거나 유효하지 않습니다."),
            @ApiResponse(responseCode = "403", description = "COMMENT003 - 해당 댓글에 대한 삭제 권한이 없습니다."),
            @ApiResponse(responseCode = "404", description = "COMMENT001 - 존재하지 않는 댓글입니다.")
    })
    @DeleteMapping("/{comment_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeComment(
            @Parameter(description = "댓글 ID") @PathVariable("comment_id") Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        commentService.deleteComment(commentId, userDetails);
    }

    @Operation(summary = "게시물 댓글 목록 조회", description = "게시물 ID로 해당 게시물의 댓글 목록을 조회합니다. 경로 변수는 댓글 ID가 아니라 게시물 ID입니다. 로그인 없이 조회 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "POST001 - 존재하지 않는 게시물입니다.")
    })
    @GetMapping("/{post_id}")
    public CommonResponse<List<CommentDTO.Response>> getComments(
            @Parameter(description = "게시물 ID") @PathVariable("post_id") Long postId
    ) {
        return CommonResponse.ok(
                commentService.getCommentsByPost(postId)
        );
    }
}
