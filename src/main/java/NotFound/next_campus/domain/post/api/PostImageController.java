package NotFound.next_campus.domain.post.api;

import NotFound.next_campus.domain.post.service.PostImageService;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "게시물 (이미지)", description = "게시물 이미지 관련 API (이미지 등록 · 수정)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostImageController {

    private final PostImageService postImageService;

    /* 이미지 등록 */
    @Operation(summary = "게시물 이미지 등록", description = "게시물에 이미지를 추가로 업로드합니다 (multipart/form-data, 필드명: files). "
            + "게시물 작성자 또는 ADMIN만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "이미지 등록 성공"),
            @ApiResponse(responseCode = "401", description = "AUTH001 - 인증 토큰이 없거나 유효하지 않습니다."),
            @ApiResponse(responseCode = "403", description = "POST007 - 해당 게시물에 대한 이미지 등록 권한이 없습니다."),
            @ApiResponse(responseCode = "404", description = "POST001 - 존재하지 않는 게시물입니다. / FILE001 - 이미지가 존재하지 않습니다. (빈 파일)"),
            @ApiResponse(responseCode = "500", description = "FILE002 - 파일 저장 실패")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{post_id}/images")
    public CommonResponse<String> registerPostImages(
            @Parameter(description = "게시물 ID") @PathVariable("post_id") Long postId,
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        postImageService.attach(postId, files, userDetails);

        return CommonResponse.ok(
                "이미지 등록 성공"
        );
    }

    /* 게시물 이미지 수정 */
    @Operation(summary = "게시물 이미지 수정", description = "게시물의 기존 이미지를 모두 삭제하고 새로 업로드한 이미지로 교체합니다 (multipart/form-data, 필드명: files). "
            + "게시물 작성자 또는 ADMIN만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "게시물 이미지 수정 성공 (응답 body 없음)"),
            @ApiResponse(responseCode = "401", description = "AUTH001 - 인증 토큰이 없거나 유효하지 않습니다."),
            @ApiResponse(responseCode = "403", description = "POST008 - 해당 게시물 이미지에 대한 수정 권한이 없습니다."),
            @ApiResponse(responseCode = "404", description = "POST001 - 존재하지 않는 게시물입니다. / FILE001 - 이미지가 존재하지 않습니다. (빈 파일)"),
            @ApiResponse(responseCode = "500", description = "FILE002 - 파일 저장 실패")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("/{post_id}/images")
    public void modifyPostImage(
            @Parameter(description = "게시물 ID") @PathVariable("post_id") Long postId,
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        postImageService.replace(postId, files, userDetails);
    }
}
