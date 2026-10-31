package NotFound.next_campus.domain.post.service;

import NotFound.next_campus.domain.member.model.Role;
import NotFound.next_campus.domain.post.model.Post;
import NotFound.next_campus.domain.post.model.PostImage;
import NotFound.next_campus.domain.post.repository.PostImageRepository;
import NotFound.next_campus.domain.post.repository.PostRepository;
import NotFound.next_campus.global.auth.user.CustomUserDetails;
import NotFound.next_campus.global.exception.BusinessException;
import NotFound.next_campus.global.exception.ErrorCode;
import NotFound.next_campus.global.firebase.service.FirebaseStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PostImageServiceImpl implements PostImageService {

    // private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final FirebaseStorageService firebaseStorageService;

    /* 전체 게시물 이미지 목록 조회 */
    @Override
    public Map<Long, List<String>> getUrls(List<Post> posts) {

        List<PostImage> images = postImageRepository.findAllByPosts(posts);

        return images.stream()
                .collect(Collectors.groupingBy(
                        pi -> pi.getPost().getId(),
                        Collectors.mapping(
                                // pi -> "/uploads/" + pi.getStoredFileName(), Collectors.toList()
                                pi -> firebaseStorageService.getPublicUrl(pi.getStoredFileName()), Collectors.toList()
                        )
                ));
    }

    /* 특정 게시물의 이미지 조회 */
    @Override
    public List<String> getUrls(Post post) {

        return postImageRepository.findByPost(post).stream()
                // .map(pi -> "/uploads/" + pi.getStoredFileName())
                .map(pi -> firebaseStorageService.getPublicUrl(pi.getStoredFileName()))
                .toList();
    }

    /* 이미지 등록 */
    @Override
    public void attach(Long postId, List<MultipartFile> files, CustomUserDetails user) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        // 권한 체크
        post.validateOwnerOrAdmin(user.getMember(), ErrorCode.POST_IMAGE_REGISTER_FORBIDDEN);

        saveImages(post, files);
    }

    /* 이미지 수정 */
    @Override
    public void replace(Long postId, List<MultipartFile> files, CustomUserDetails user) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        // 권한 체크
        post.validateOwnerOrAdmin(user.getMember(), ErrorCode.POST_IMAGE_UPDATE_FORBIDDEN);

        List<PostImage> images = postImageRepository.findByPost(post);
        saveImages(post, files);    // 신규 이미지 등록
        deleteImages(images);       // 기존 이미지 삭제
    }

    /* 이미지 삭제 */
    @Override
    public void deleteAllOf(Post post) {

        deleteAllOf(List.of(post));
    }

    /* 이미지 일괄 삭제 */
    @Override
    public void deleteAllOf(List<Post> posts) {

        if(posts.isEmpty()) return;

        List<PostImage> images = postImageRepository.findAllByPosts(posts);
        deleteImages(images);
    }

    /* 이미지 여러개 동시 등록 */
    private List<String> saveImages(Post post, List<MultipartFile> files) {

        List<String> images = new ArrayList<>();

        for (MultipartFile file : files) {

            String path = saveImage(post, file);
            images.add(path);
        }

        return images;
    }

    // Firebase
    private String saveImage(Post post, MultipartFile file) {

        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.IMAGE_NOT_FOUND);
        }

        String originalFileName = file.getOriginalFilename();
        String storedFileName = firebaseStorageService.upload(file);

        postImageRepository.save(PostImage.builder()
                .post(post)
                .originalFileName(originalFileName)
                .storedFileName(storedFileName)
                .build());

        return firebaseStorageService.getPublicUrl(storedFileName);
    }

    /* 이미지 여러개 동시 삭제 */
    private void deleteImages(List<PostImage> images) {

        for (PostImage image : images) {

            deleteImage(image);
        }

        postImageRepository.deleteAll(images);
    }

    // Firebase
    private void deleteImage(PostImage image) {

        firebaseStorageService.delete(image.getStoredFileName());
    }
}
