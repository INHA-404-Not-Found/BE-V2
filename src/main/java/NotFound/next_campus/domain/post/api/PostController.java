package NotFound.next_campus.domain.post.api;

import NotFound.next_campus.domain.post.dto.PostDTO;
import NotFound.next_campus.domain.post.model.PostStatus;
import NotFound.next_campus.domain.post.model.PostType;
import NotFound.next_campus.domain.post.service.PostService;
import NotFound.next_campus.global.auth.user.CustomUserDetails;
import NotFound.next_campus.global.common.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "게시물", description = "게시물 API (게시물 / 이미지 등록 · 수정 · 삭제 · 조회)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시물 등록", description = "분실/습득/공지 게시물을 등록하고 생성된 게시물 ID를 반환합니다. "
            + "습득(FIND) 게시물은 발견 위치(locationId)가 필수이며, 등록 시 같은 카테고리의 분실 신고자에게 알림이 전송됩니다. "
            + "개인 분실물(isPersonal=true)인 경우 해당 학번의 학생에게 안내 메일이 발송됩니다. "
            + "공지(NOTICE) 등록과 인계(POLICE) 상태 지정은 ADMIN만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "게시물 등록 성공 (data: 게시물 ID)"),
            @ApiResponse(responseCode = "401", description = "AUTH001 - 인증 토큰이 없거나 유효하지 않습니다."),
            @ApiResponse(responseCode = "403", description = "POST003 - 인계 상태 등록 권한이 없습니다. / POST005 - 공지는 관리자만 등록할 수 있습니다."),
            @ApiResponse(responseCode = "404", description = "LOCATION001 - 존재하지 않는 위치입니다. / POST002 - 해당 학번의 학생을 찾을 수 없습니다.")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<Long> registerPost(
            @RequestBody PostDTO.CreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {

        return CommonResponse.ok(
                postService.savePost(request, userDetails)
        );
    }

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
    @PostMapping("/{post_id}/images")
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<String> registerPostImages(
            @Parameter(description = "게시물 ID") @PathVariable("post_id") Long postId,
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        postService.savePostImages(postId, files, userDetails);

        return CommonResponse.ok(
                "이미지 등록 성공"
        );
    }

    /* 게시물 수정 */
    @Operation(summary = "게시물 수정", description = "게시물 내용을 부분 수정합니다. null인 필드는 변경하지 않으며, categories를 보내면 기존 카테고리를 모두 교체합니다. "
            + "게시물 작성자 또는 ADMIN만 가능하고, 인계(POLICE) 상태 및 공지(NOTICE) 유형으로의 변경은 ADMIN만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "게시물 수정 성공 (응답 body 없음)"),
            @ApiResponse(responseCode = "401", description = "AUTH001 - 인증 토큰이 없거나 유효하지 않습니다."),
            @ApiResponse(responseCode = "403", description = "POST009 - 해당 게시물에 대한 수정 권한이 없습니다. / POST004 - 인계 상태 수정 권한이 없습니다. / POST006 - 공지 게시 권한이 없습니다."),
            @ApiResponse(responseCode = "404", description = "POST001 - 존재하지 않는 게시물입니다. / LOCATION001 - 존재하지 않는 위치입니다.")
    })
    @PatchMapping("/{post_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void modifyPost(
            @Parameter(description = "게시물 ID") @PathVariable("post_id") Long postId,
            @RequestBody PostDTO.UpdateContentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        postService.updatePost(postId, request, userDetails);
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
    @PatchMapping("/{post_id}/images")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void modifyPostImage(
            @Parameter(description = "게시물 ID") @PathVariable("post_id") Long postId,
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        postService.updatePostImages(postId, files, userDetails);
    }

    /* 게시물 상태 일괄 수정 */
    @Operation(summary = "게시물 상태 일괄 수정", description = "postIds에 해당하는 게시물들의 상태(status)를 한 번에 변경합니다. "
            + "존재하지 않는 ID는 무시됩니다. ADMIN 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "게시물 상태 일괄 수정 성공 (응답 body 없음)"),
            @ApiResponse(responseCode = "401", description = "AUTH001 - 인증 토큰이 없거나 유효하지 않습니다."),
            @ApiResponse(responseCode = "403", description = "POST011 - 게시물 일괄 수정 권한이 없습니다.")
    })
    @PatchMapping("/update")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void modifyPosts(
            @RequestBody PostDTO.UpdateStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        postService.updateStatusOfPosts(request, userDetails);
    }

    /* 게시물 삭제 */
    @Operation(summary = "게시물 삭제", description = "게시물과 첨부 이미지를 삭제합니다. 게시물 작성자 또는 ADMIN만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "게시물 삭제 성공 (응답 body 없음)"),
            @ApiResponse(responseCode = "401", description = "AUTH001 - 인증 토큰이 없거나 유효하지 않습니다."),
            @ApiResponse(responseCode = "403", description = "POST010 - 해당 게시물에 대한 삭제 권한이 없습니다."),
            @ApiResponse(responseCode = "404", description = "POST001 - 존재하지 않는 게시물입니다.")
    })
    @DeleteMapping("/{post_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removePost(
            @Parameter(description = "게시물 ID") @PathVariable("post_id") Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        postService.deletePost(postId, userDetails);
    }

    /* 게시물 일괄 삭제 */
    @Operation(summary = "게시물 일괄 삭제", description = "postIds에 해당하는 게시물들을 한 번에 삭제합니다. ADMIN 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "게시물 일괄 삭제 성공 (응답 body 없음)"),
            @ApiResponse(responseCode = "401", description = "AUTH001 - 인증 토큰이 없거나 유효하지 않습니다."),
            @ApiResponse(responseCode = "403", description = "POST012 - 게시물 일괄 삭제 권한이 없습니다.")
    })
    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removePosts(
            @RequestBody PostDTO.DeleteRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        postService.deletePosts(request.getPostIds(), userDetails);
    }

    @Operation(summary = "게시물 단건 조회", description = "게시물 ID로 게시물 상세(카테고리 이름 목록, 이미지 URL 목록 포함)를 조회합니다. 로그인 없이 조회 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "POST001 - 존재하지 않는 게시물입니다.")
    })
    @GetMapping("/{post_id}")
    @SecurityRequirements
    public CommonResponse<PostDTO.Response> getPost(
            @Parameter(description = "게시물 ID") @PathVariable("post_id") Long postId
    ) {
        return CommonResponse.ok(
                postService.getPostById(postId)
        );
    }

    @Operation(summary = "게시물 전체 조회", description = "전체 게시물을 최신순으로 페이지당 10개씩 조회합니다. 로그인 없이 조회 가능합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    @SecurityRequirements
    public CommonResponse<List<PostDTO.Response>> getAllPosts(
            @Parameter(hidden = true) @PageableDefault(page = 1) Pageable pageable,
            @Parameter(description = "페이지 번호 (1부터 시작, 생략 시 1)")
            @RequestParam(required = false, defaultValue = "0", value = "page") int pageNo
    ) {
        pageNo = (pageNo == 0) ? 0 : pageNo - 1;

        return CommonResponse.ok(
                postService.getAllPostList(pageable, pageNo)
        );
    }

    @Operation(summary = "태그별 게시물 조회", description = "상태 · 유형 · 위치 · 카테고리 조건으로 게시물을 필터링해 최신순으로 페이지당 10개씩 조회합니다. "
            + "조건은 모두 선택값이며, 생략한 조건은 필터링하지 않습니다. 로그인 없이 조회 가능합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/tags")
    @SecurityRequirements
    public CommonResponse<List<PostDTO.Response>> getPostsByTags(
            @Parameter(description = "게시물 상태") @RequestParam(value = "status", required = false) PostStatus status,
            @Parameter(description = "게시물 유형") @RequestParam(value = "type", required = false) PostType type,
            @Parameter(description = "위치 ID") @RequestParam(value = "location_id", required = false) Long locationId,
            @Parameter(description = "카테고리 ID") @RequestParam(value = "category_id", required = false) Long categoryId,
            @Parameter(hidden = true) @PageableDefault(page = 1) Pageable pageable,
            @Parameter(description = "페이지 번호 (1부터 시작, 생략 시 1)")
            @RequestParam(required = false, defaultValue = "0", value = "page") int pageNo
    ) {
        pageNo = (pageNo == 0) ? 0 : pageNo - 1;

        return CommonResponse.ok(
                postService.getPostsByTags(status, type, locationId, categoryId,
                        pageable, pageNo)
        );
    }

    @Operation(summary = "키워드 게시물 검색", description = "키워드로 게시물을 검색해 최신순으로 페이지당 10개씩 조회합니다. 로그인 없이 조회 가능합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/search")
    @SecurityRequirements
    public CommonResponse<List<PostDTO.Response>> getPostsByKeyword(
            @Parameter(description = "검색 키워드") @RequestParam("keyword") String keyword,
            @Parameter(hidden = true) @PageableDefault(page = 1) Pageable pageable,
            @Parameter(description = "페이지 번호 (1부터 시작, 생략 시 1)")
            @RequestParam(required = false, defaultValue = "0", value = "page") int pageNo
    ) {
        pageNo = (pageNo == 0) ? 0 : pageNo - 1;

        return CommonResponse.ok(
                postService.getPostsByKeyword(keyword, pageable, pageNo)
        );
    }

    @Operation(summary = "내 게시물 조회", description = "로그인한 사용자가 작성한 게시물을 최신순으로 페이지당 10개씩 조회합니다. 로그인이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "AUTH001 - 인증 토큰이 없거나 유효하지 않습니다.")
    })
    @GetMapping("/my")
    public CommonResponse<List<PostDTO.Response>> getMyPosts(
            @Parameter(hidden = true) @PageableDefault(page = 1) Pageable pageable,
            @Parameter(description = "페이지 번호 (1부터 시작, 생략 시 1)")
            @RequestParam(required = false, defaultValue = "0", value = "page") int pageNo,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        pageNo = (pageNo == 0) ? 0 : pageNo - 1;

        return CommonResponse.ok(
                postService.getMyPosts(pageable, pageNo, userDetails)
        );
    }

    @Operation(summary = "키워드 + 태그 게시물 검색", description = "키워드 검색 결과를 상태 · 유형 · 위치 · 카테고리 조건으로 추가 필터링해 최신순으로 페이지당 10개씩 조회합니다. "
            + "태그 조건은 모두 선택값입니다. 로그인 없이 조회 가능합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/search/tags")
    @SecurityRequirements
    public CommonResponse<List<PostDTO.Response>> getPostsByKeywordAndTags(
            @Parameter(description = "검색 키워드") @RequestParam("keyword") String keyword,
            @Parameter(description = "게시물 상태") @RequestParam(value = "status", required = false) PostStatus status,
            @Parameter(description = "게시물 유형") @RequestParam(value = "type", required = false) PostType type,
            @Parameter(description = "위치 ID") @RequestParam(value = "location_id", required = false) Long locationId,
            @Parameter(description = "카테고리 ID") @RequestParam(value = "category_id", required = false) Long categoryId,
            @Parameter(hidden = true) @PageableDefault(page = 1) Pageable pageable,
            @Parameter(description = "페이지 번호 (1부터 시작, 생략 시 1)")
            @RequestParam(required = false, defaultValue = "0", value = "page") int pageNo
    ) {
        pageNo = (pageNo == 0) ? 0 : pageNo - 1;

        return CommonResponse.ok(
                postService.getPostsByKeywordAndTags(
                        keyword, status, type, locationId, categoryId,
                        pageable, pageNo)
        );
    }
}
