package NotFound.next_campus.domain.post.service;

import NotFound.next_campus.domain.post.dto.PostDTO;
import NotFound.next_campus.domain.post.model.PostStatus;
import NotFound.next_campus.domain.post.model.PostType;
import NotFound.next_campus.global.auth.user.CustomUserDetails;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostService {

    /* 게시물 등록 */
    Long savePost(PostDTO.CreateRequest dto, CustomUserDetails userDetails);
    /* 게시물 수정 */
    Long updatePost(Long postId, PostDTO.UpdateContentRequest dto, CustomUserDetails userDetails);
    /* 게시물 인계 여부 일괄 수정 */
    void updateStatusOfPosts(PostDTO.UpdateStatusRequest dto);

    /* 게시물 삭제 */
    void deletePost(Long postId, CustomUserDetails userDetails);
    /* 게시물 일괄 삭제 */
    void deletePosts(List<Long> postIds);

    /* 특정 게시물 내용 조회 */
    PostDTO.Response getPostById(Long postId);

    /* 전체 게시물 목록 조회 */
    List<PostDTO.Response> getAllPostList(Pageable pageable, int pageNo);

    /* 게시물 목록 필터링 조회 */
    List<PostDTO.Response> getPostsByTags(PostStatus status, PostType type, Long locationId, Long categoryId,
                                         Pageable pageable, int pageNo);

    /* 게시물 키워드 검색 */
    List<PostDTO.Response> getPostsByKeyword(String keyword, Pageable pageable, int pageNo);

    /* 내가 올린 게시물 목록 조회 */
    List<PostDTO.Response> getMyPosts(Pageable pageable, int pageNo, CustomUserDetails userDetails);

    List<PostDTO.Response> getPostsByKeywordAndTags(String keyword, PostStatus status, PostType type, Long locationId, Long categoryId,
                                                    Pageable pageable, int pageNo);
}
