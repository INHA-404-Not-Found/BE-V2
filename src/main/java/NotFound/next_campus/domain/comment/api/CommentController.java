package NotFound.next_campus.domain.comment.api;

import NotFound.next_campus.domain.comment.dto.CommentDTO;
import NotFound.next_campus.domain.comment.service.CommentService;
import NotFound.next_campus.global.auth.user.CustomUserDetails;
import NotFound.next_campus.global.common.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public CommonResponse<Map<String, Object>> registerComment(
            @RequestBody CommentDTO.CreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long commentId = commentService.saveComment(request, userDetails);

        return CommonResponse.ok(
                Map.of(
                        "message", "댓글 등록 성공",
                        "commentId", commentId
                )
        );
    }

    @PatchMapping("/{comment_id}")
    public CommonResponse<String> updateComment(
            @PathVariable("comment_id") Long commentId,
            @RequestBody CommentDTO.UpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        commentService.updateComment(commentId, request, userDetails);

        return CommonResponse.ok(
                "댓글 수정 성공"
        );
    }

    @DeleteMapping("/{comment_id}")
    public CommonResponse<String> removeComment(
            @PathVariable("comment_id") Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        commentService.deleteComment(commentId, userDetails);

        return CommonResponse.ok(
                "댓글 삭제 성공"
        );
    }

    @GetMapping("/{post_id}")
    public CommonResponse<List<CommentDTO.Response>> getComments(
            @PathVariable("post_id") Long postId
    ) {
        return CommonResponse.ok(
                commentService.getCommentsByPost(postId)
        );
    }
}
