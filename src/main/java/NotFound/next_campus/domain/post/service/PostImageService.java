package NotFound.next_campus.domain.post.service;

import NotFound.next_campus.domain.post.model.Post;
import NotFound.next_campus.global.auth.user.CustomUserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface PostImageService {

    /* 조회 - PostServiceImpl이 호출 */
    Map<Long, List<String>> getUrls(List<Post> posts);
    List<String> getUrls(Post post);

    /* 등록, 수정 - Controller에서 호출 */
    void attach(Long postId, List<MultipartFile> files, CustomUserDetails user);
    void replace(Long postId, List<MultipartFile> files, CustomUserDetails user);

    /* 삭제 - PostServiceImpl이 호출 */
    void deleteAllOf(Post post);
    void deleteAllOf(List<Post> posts);
}
