package NotFound.next_campus.domain.post.api;

import NotFound.next_campus.domain.post.dto.PostDTO;
import NotFound.next_campus.domain.post.model.PostStatus;
import NotFound.next_campus.domain.post.model.PostType;
import NotFound.next_campus.domain.post.service.PostService;
import NotFound.next_campus.global.auth.user.CustomUserDetails;
import NotFound.next_campus.global.common.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    @Operation(summary = "게시글 등록")
    @ApiResponse(responseCode = "201", description = "게시글 등록 성공")
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

    @PostMapping("/{post_id}/images")
    public CommonResponse<String> registerPostImage(
            @PathVariable("post_id") Long postId,
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        postService.savePostImages(postId, files, userDetails);

        return CommonResponse.ok(
                "이미지 등록 성공"
        );
    }

    @PatchMapping("/{post_id}")
    public CommonResponse<String> modifyPost(
            @PathVariable("post_id") Long postId,
            @RequestBody PostDTO.UpdateContentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        postService.updatePost(postId, request, userDetails);

        return CommonResponse.ok(
                "게시물 수정 성공"
        );
    }

    @PatchMapping("/{post_id}/images")
    public CommonResponse<String> modifyPostImage(
            @PathVariable("post_id") Long postId,
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        postService.updatePostImages(postId, files, userDetails);

        return CommonResponse.ok(
                "게시물 이미지 수정 성공"
        );
    }

    @PatchMapping("/update")
    public CommonResponse<String> modifyPosts(
            @RequestBody PostDTO.UpdateStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        postService.updateStatusOfPosts(request, userDetails);

        return CommonResponse.ok(
                "게시물 인계 여부 일괄 수정 성공"
        );
    }

    @DeleteMapping("/{post_id}")
    public CommonResponse<String> removePost(
            @PathVariable("post_id") Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        postService.deletePost(postId, userDetails);

        return CommonResponse.ok(
                "게시물 삭제 성공"
        );
    }

    @PostMapping("/delete")
    public CommonResponse<String> removePosts(
            @RequestBody PostDTO.DeleteRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        postService.deletePosts(request.getPostIds(), userDetails);

        return CommonResponse.ok(
                "게시물 일괄 삭제 성공"
        );
    }

    @GetMapping("/{post_id}")
    public CommonResponse<PostDTO.Response> getPost(
            @PathVariable("post_id") Long postId
    ) {
        return CommonResponse.ok(
                postService.getPostById(postId)
        );
    }

    @GetMapping
    public CommonResponse<List<PostDTO.Response>> getAllPosts(
            @PageableDefault(page = 1) Pageable pageable,
            @RequestParam(required = false, defaultValue = "0", value = "page") int pageNo
    ) {
        pageNo = (pageNo == 0) ? 0 : pageNo - 1;

        return CommonResponse.ok(
                postService.getAllPostList(pageable, pageNo)
        );
    }

    @GetMapping("/tags")
    public CommonResponse<List<PostDTO.Response>> getPostsByTags(
            @RequestParam(value = "status", required = false) PostStatus status,
            @RequestParam(value = "type", required = false) PostType type,
            @RequestParam(value = "location_id", required = false) Long locationId,
            @RequestParam(value = "category_id", required = false) Long categoryId,
            @PageableDefault(page = 1) Pageable pageable,
            @RequestParam(required = false, defaultValue = "0", value = "page") int pageNo
    ) {
        pageNo = (pageNo == 0) ? 0 : pageNo - 1;

        return CommonResponse.ok(
                postService.getPostsByTags(status, type, locationId, categoryId,
                        pageable, pageNo)
        );
    }

    @GetMapping("/search")
    public CommonResponse<List<PostDTO.Response>> getPostsByKeyword(
            @RequestParam("keyword") String keyword,
            @PageableDefault(page = 1) Pageable pageable,
            @RequestParam(required = false, defaultValue = "0", value = "page") int pageNo
    ) {
        pageNo = (pageNo == 0) ? 0 : pageNo - 1;

        return CommonResponse.ok(
                postService.getPostsByKeyword(keyword, pageable, pageNo)
        );
    }

    @GetMapping("/my")
    public CommonResponse<List<PostDTO.Response>> getMyPosts(
            @PageableDefault(page = 1) Pageable pageable,
            @RequestParam(required = false, defaultValue = "0", value = "page") int pageNo,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        pageNo = (pageNo == 0) ? 0 : pageNo - 1;

        return CommonResponse.ok(
                postService.getMyPosts(pageable, pageNo, userDetails)
        );
    }

    @GetMapping("/search/tags")
    public CommonResponse<List<PostDTO.Response>> getPostsByKeywordAndTags(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "status", required = false) PostStatus status,
            @RequestParam(value = "type", required = false) PostType type,
            @RequestParam(value = "location_id", required = false) Long locationId,
            @RequestParam(value = "category_id", required = false) Long categoryId,
            @PageableDefault(page = 1) Pageable pageable,
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
