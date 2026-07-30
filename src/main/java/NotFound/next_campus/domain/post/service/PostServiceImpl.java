package NotFound.next_campus.domain.post.service;

import NotFound.next_campus.domain.category.model.Category;
import NotFound.next_campus.domain.category.repository.CategoryRepository;
import NotFound.next_campus.domain.location.model.Location;
import NotFound.next_campus.domain.location.repository.LocationRepository;
import NotFound.next_campus.domain.member.model.Member;
import NotFound.next_campus.domain.member.model.Role;
import NotFound.next_campus.domain.member.repository.MemberRepository;
import NotFound.next_campus.domain.notification.dto.NotificationDTO;
import NotFound.next_campus.domain.notification.service.NotificationService;
import NotFound.next_campus.domain.post.dto.PostDTO;
import NotFound.next_campus.domain.post.model.*;
import NotFound.next_campus.domain.post.repository.PostCategoryRepository;
import NotFound.next_campus.domain.post.repository.PostImageRepository;
import NotFound.next_campus.domain.post.repository.PostRepository;
import NotFound.next_campus.global.auth.user.CustomUserDetails;
import NotFound.next_campus.global.firebase.service.FirebaseStorageService;
import NotFound.next_campus.global.mail.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PostServiceImpl implements PostService {

    // private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    private final MemberRepository memberRepository;
    private final MailService mailService;
    private final LocationRepository locationRepository;
    private final CategoryRepository categoryRepository;

    private final PostRepository postRepository;
    private final PostCategoryRepository postCategoryRepository;
    private final PostImageRepository postImageRepository;

    private final NotificationService notificationService;
    private final FirebaseStorageService firebaseStorageService;

    private static int PAGE_LIMIT = 10;

    @Override
    public Long savePost(PostDTO.CreateRequest dto, CustomUserDetails userDetails) {

        Member member = userDetails.getMember();

        Post post = Post.builder()
                .member(member)
                .title(dto.getTitle())
                .content(dto.getContent())
                .type(dto.getType())
                .isPersonal(dto.getIsPersonal())
                .build();

        // 게시물 상태가 POLICE 인 경우
        if (Role.USER.equals(member.getRole())
                && PostStatus.POLICE.equals(dto.getStatus())) {
            throw new AccessDeniedException("인계 상태 등록 권한이 없습니다.");
        }

        // 완료/미완료/인계
        post.setStatus(dto.getStatus());

        // 공지사항인 경우
        if (PostType.NOTICE.equals(dto.getType())
                && !Role.ADMIN.equals(member.getRole())) {

            throw new AccessDeniedException("공지는 관리자만 등록할 수 있습니다.");
        }

        // 습득 게시물인 경우
        if (PostType.FIND.equals(dto.getType())) {
            // 분실물 발견 위치
            Location location = locationRepository.findById(dto.getLocationId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장소입니다."));

            // 개인 정보(학번)가 포함된 분실물인 경우
            if (Boolean.TRUE.equals(dto.getIsPersonal()) &&
                    dto.getStudentId() != null &&
                    !dto.getStudentId().isEmpty()) {

                post.setStudentId(dto.getStudentId());
            }

            post.setLocation(location);                         // 발견 장소
            post.setLocationDetail(dto.getLocationDetail());    // 세부 발견 장소
            post.setStoredLocation(dto.getStoredLocation());    // 보관장소
        }

        // 게시물 저장
        postRepository.save(post);

        savePostCategory(post, dto.getCategories());

        // 습득 게시물인 경우, 동일 카테고리의 분실 신고자 목록에 알림 전송
        if(PostType.FIND.equals(post.getType())) {
            sendLostPostMatchNotification(post, member);
        }

        // 분실물(isPersonal=true)인 경우, 해당 학생에게 이메일 발송
        if (Boolean.TRUE.equals(post.getIsPersonal())) {
            Member targetStudent = memberRepository.findByStudentId(Long.valueOf(post.getStudentId()))
                    .orElseThrow(() -> new IllegalArgumentException("해당 학번의 학생을 찾을 수 없습니다."));
            mailService.sendPersonalLostEmail(
                    targetStudent.getEmail(),
                    targetStudent.getName(),
                    post.getTitle(),
                    post.getCreatedAt()
            );
        }
      
        return post.getId();
    }

    @Override
    public void savePostImages(Long postId, List<MultipartFile> files, CustomUserDetails userDetails) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물입니다."));

        if (!post.getMember().equals(userDetails.getMember()) &&
                !Role.ADMIN.equals(userDetails.getRole())) {
            throw new AccessDeniedException("해당 게시물에 대한 이미지 등록 권한이 없습니다.");
        }

        saveImages(post, files);
    }

    @Override
    public Long updatePost(Long postId, PostDTO.UpdateContentRequest dto, CustomUserDetails userDetails) {

        Member member = userDetails.getMember();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물입니다."));

        // 해당 게시물의 작성자도 아니고, 관리자도 아닌 경우
        if (!post.getMember().equals(member) &&
                !Role.ADMIN.equals(member.getRole())) {
            throw new AccessDeniedException("해당 게시물에 대한 수정 권한이 없습니다.");
        }

        if (dto.getLocationDetail() != null) post.setLocationDetail(dto.getLocationDetail());
        if (dto.getTitle() != null) post.setTitle(dto.getTitle());
        if (dto.getContent() != null) post.setContent(dto.getContent());
        if (dto.getStoredLocation() != null) post.setStoredLocation(dto.getStoredLocation());
        if (dto.getIsPersonal() != null) post.setIsPersonal(dto.getIsPersonal());

        // 발견 위치 수정
        if (dto.getLocationId() != null) {

            Location location = locationRepository.findById(dto.getLocationId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장소입니다."));

            post.setLocation(location);
        }

        // 게시물 상태 수정
        if (dto.getStatus() != null) {

            // 일반 사용자는 인계 상태를 수정할 수 없음
            if (Role.USER.equals(member.getRole()) &&
                    PostStatus.POLICE.equals(dto.getStatus())) {
                throw new AccessDeniedException("인계 상태 수정 권한이 없습니다.");
            }

            post.setStatus(dto.getStatus());
        }

        // 게시물 유형 수정
        if (dto.getType() != null) {

            // 일반 사용자는 공지를 게시할 수 없음
            if (Role.USER.equals(member.getRole()) &&
                    PostType.NOTICE.equals(dto.getType())) {
                throw new AccessDeniedException("공지 게시 권한이 없습니다.");
            }

            post.setType(dto.getType());
        }

        // 게시물 카테고리 수정
        if (dto.getCategories() != null) {

            // 기존 카테고리 삭제
            postCategoryRepository.deleteByPost(post);

            // 새 카테고리 저장
            savePostCategory(post, dto.getCategories());
        }

        return post.getId();
    }

    @Override
    public void updatePostImages(Long postId, List<MultipartFile> files, CustomUserDetails userDetails) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물입니다."));

        if (!post.getMember().equals(userDetails.getMember()) &&
                !Role.ADMIN.equals(userDetails.getRole())) {
            throw new AccessDeniedException("해당 게시물 이미지에 대한 수정 권한이 없습니다.");
        }

        List<PostImage> images = postImageRepository.findByPost(post);

        deleteImages(images);
        saveImages(post, files);
    }

    @Override
    public void updateStatusOfPosts(PostDTO.UpdateStatusRequest dto, CustomUserDetails userDetails) {

        Member member = userDetails.getMember();

        if (!Role.ADMIN.equals(member.getRole())) {
            throw new IllegalArgumentException("게시물 일괄 수정 권한이 없습니다.");
        }

        List<Post> posts = postRepository.findAllById(dto.getPostIds());

        for (Post p : posts) {

            if (dto.getStatus() != null) p.setStatus(dto.getStatus());
        }
    }

    @Override
    public void deletePost(Long postId, CustomUserDetails userDetails) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물입니다 ."));

        if (!post.getMember().equals(userDetails.getMember()) &&
                !Role.ADMIN.equals(userDetails.getRole())) {
            throw new IllegalArgumentException("해당 게시물에 대한 삭제 권한이 없습니다.");
        }

        List<PostImage> images = postImageRepository.findByPost(post);
        // 이미지 삭제
        deleteImages(images);
        // 게시물 삭제
        postRepository.delete(post);
    }

    @Override
    public void deletePosts(List<Long> postIds, CustomUserDetails userDetails) {

        if (!Role.ADMIN.equals(userDetails.getRole())) {
            throw new IllegalArgumentException("게시물 일괄 삭제 권한이 없습니다.");
        }

        postRepository.deleteAllById(postIds);
    }

    @Override
    public PostDTO.Response getPostById(Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물입니다 ."));

        List<String> categories = postCategoryRepository.findByPost(post).stream()
                .map(pc -> pc.getCategory().getName())
                .toList();

        List<String> images = postImageRepository.findByPost(post).stream()
                // .map(pi -> "/uploads/" + pi.getStoredFileName())
                .map(pi -> firebaseStorageService.getPublicUrl(pi.getStoredFileName()))
                .toList();

        return PostDTO.Response.from(post, categories, images);
    }

    /* 전체 조회 */
    @Override
    public List<PostDTO.Response> getAllPostList(Pageable pageable, int pageNo) {

        pageable = PageRequest.of(pageNo, PAGE_LIMIT, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 전체 게시물 목록 가져오기
        Page<Post> postPage = postRepository.findAll(pageable);

        List<Post> posts = postPage.getContent();

        return getPostResponses(posts);
    }

    /* 필터링 검색 */
    @Override
    public List<PostDTO.Response> getPostsByTags(PostStatus status, PostType type,
                                                Long locationId, Long categoryId,
                                                Pageable pageable, int pageNo) {

        pageable = PageRequest.of(pageNo, PAGE_LIMIT, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Post> postPage = postRepository.findPostsByTags(status, type, locationId, categoryId, pageable);

        List<Post> posts = postPage.getContent();

        return getPostResponses(posts);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostDTO.Response> getPostsByKeyword(String keyword, Pageable pageable, int pageNo) {

        pageable = PageRequest.of(pageNo, PAGE_LIMIT, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Post> postPage = postRepository.findAllSearch(keyword, pageable);

        List<Post> posts = postPage.getContent();

        return getPostResponses(posts);
    }


    @Override
    public List<PostDTO.Response> getMyPosts(Pageable pageable, int pageNo, CustomUserDetails userDetails) {

        pageable = PageRequest.of(pageNo, PAGE_LIMIT, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Post> postPage = postRepository.findByMember(userDetails.getMember(), pageable);

        List<Post> posts = postPage.getContent();

        return getPostResponses(posts);
    }

    @Override
    public List<PostDTO.Response> getPostsByKeywordAndTags(String keyword, PostStatus status, PostType type, Long locationId, Long categoryId,
                                                           Pageable pageable, int pageNo) {

        pageable = PageRequest.of(pageNo, PAGE_LIMIT, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Post> postPage = postRepository.findPostsByKeywordAndTags(keyword, status, type, locationId, categoryId, pageable);

        List<Post> posts = postPage.getContent();

        return getPostResponses(posts);
    }

    private List<PostDTO.Response> getPostResponses(List<Post> posts) {

        List<PostDTO.Response> responses = new ArrayList<>();

        // 전체 게시물의 카테고리 목록 가져오기
        List<PostCategory> categories = postCategoryRepository.findAllByPosts(posts);
        List<PostImage> images = postImageRepository.findAllByPosts(posts);

        // (게시물 id, List<카테고리명>)
        Map<Long, List<String>> allPostCategories = categories.stream()
                .collect(Collectors.groupingBy(
                        pc -> pc.getPost().getId(),
                        Collectors.mapping(
                                pc -> pc.getCategory().getName(), Collectors.toList()
                        )
                ));

        // (게시물 id, 이미지 url)
        Map<Long, List<String>> allPostImages = images.stream()
                .collect(Collectors.groupingBy(
                        pi -> pi.getPost().getId(),
                        Collectors.mapping(
                                // pi -> "/uploads/" + pi.getStoredFileName(), Collectors.toList()
                                pi -> firebaseStorageService.getPublicUrl(pi.getStoredFileName()), Collectors.toList()
                        )
                ));

        for (Post p : posts) {
            List<String> categoriesOfPost = allPostCategories.getOrDefault(p.getId(), List.of());
            List<String> imagesOfPost = allPostImages.getOrDefault(p.getId(), List.of());
            responses.add(PostDTO.Response.from(p, categoriesOfPost, imagesOfPost));
        }

        return responses;
    }

    private void savePostCategory(Post post, List<Long> categories) {

        // 게시물 카테고리 저장
        List<Category> categoryList = categoryRepository.findAllById(categories);

        for (Category c : categoryList) {

            postCategoryRepository.save(PostCategory.builder()
                    .post(post)
                    .category(c)
                    .build());
        }
    }

    private List<String> saveImages(Post post, List<MultipartFile> files) {

        List<String> images = new ArrayList<>();

        for (MultipartFile file : files) {

            String path = saveImage(post, file);
            images.add(path);
        }

        return images;
    }

    // 로컬 서버 저장 방식
    /*
    private String saveImage(Post post, MultipartFile file) {

         if (file.isEmpty()) {
             throw new IllegalArgumentException("이미지가 존재하지 않습니다.");
         }

         String originalFileName = file.getOriginalFilename();
         String storedFileName = UUID.randomUUID() + "_" + originalFileName;

         String path = UPLOAD_DIR + storedFileName;

         try {
             File destination = new File(path);
             destination.getParentFile().mkdirs();
             file.transferTo(destination);
         } catch (IOException e) {
             throw new IllegalArgumentException("파일 저장 실패");
         }

         postImageRepository.save(PostImage.builder()
                 .post(post)
                 .originalFileName(originalFileName)
                 .storedFileName(storedFileName)
                 .build());

         return "/uploads/" + storedFileName;
     }
    */

    // Firebase
    private String saveImage(Post post, MultipartFile file) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("이미지가 존재하지 않습니다.");
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

    private void deleteImages(List<PostImage> images) {

        for (PostImage image : images) {

            deleteImage(image);
        }

        postImageRepository.deleteAll(images);
    }

    /*
     private void deleteImage(PostImage image) {

         String oldFileName = image.getStoredFileName();
         File oldFile = new File(UPLOAD_DIR + oldFileName);
         if (oldFile.exists()) {
             oldFile.delete();
         }
     }
    */

    // Firebase
    private void deleteImage(PostImage image) {

        firebaseStorageService.delete(image.getStoredFileName());
    }

    // 신규 습득 게시물 등록 시, 같은 카테고리의 분실 신고자들에게 알림 전송
    private void sendLostPostMatchNotification(Post findPost, Member writer) {

        // 등록된 습득 게시물의 카테고리 목록
        List<Long> categoryIds = postCategoryRepository.findCategoryIdsByPostId(findPost.getId());

        // 해당 카테고리 목록에 속하는 분실 신고자 목록(분실 신고이면서, 카테고리 목록이 하나라도 겹치면서, 미완료인 분실 신고건)
        // 내가 올린 분실건은 제외
        List<Member> targetMembers = postRepository.findDistinctMembersByCategoryIdsAndType(
                categoryIds, PostType.LOST, PostStatus.UNCOMPLETED, writer);

        if (targetMembers.isEmpty()) {
            log.info("[알림 없음] 일치하는 분실 게시물이 없습니다.");
            return;
        }

        String title = "습득 게시물 등록 알림";
        String message = "회원님의 분실물 카테고리와 일치하는 습득 게시물이 등록되었습니다.";
        String link = "/posts/" + findPost.getId();

        for (Member lostMember : targetMembers) {

            notificationService.sendAndSaveNotification(NotificationDTO.CreateRequest.builder()
                    .memberId(lostMember.getId())
                    .title(title)
                    .message(message)
                    .link(link)
                    .build());
        }

        log.info("[알림 전송 완료] 관련 분실자 수: {}", targetMembers.size());
    }
}
