package NotFound.next_campus.domain.post.service;

import NotFound.next_campus.domain.category.model.Category;
import NotFound.next_campus.domain.category.repository.CategoryRepository;
import NotFound.next_campus.domain.location.model.Location;
import NotFound.next_campus.domain.location.repository.LocationRepository;
import NotFound.next_campus.domain.member.model.Member;
import NotFound.next_campus.domain.member.model.Role;
import NotFound.next_campus.domain.member.repository.MemberRepository;
import NotFound.next_campus.domain.post.dto.PostDTO;
import NotFound.next_campus.domain.post.event.LostItemMatchNotificationEvent;
import NotFound.next_campus.domain.post.event.PersonalLostMailEvent;
import NotFound.next_campus.domain.post.model.Post;
import NotFound.next_campus.domain.post.model.PostCategory;
import NotFound.next_campus.domain.post.model.PostStatus;
import NotFound.next_campus.domain.post.model.PostType;
import NotFound.next_campus.domain.post.repository.PostCategoryRepository;
import NotFound.next_campus.domain.post.repository.PostRepository;
import NotFound.next_campus.global.auth.user.CustomUserDetails;
import NotFound.next_campus.global.exception.BusinessException;
import NotFound.next_campus.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PostServiceImpl implements PostService {

    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final LocationRepository locationRepository;
    private final CategoryRepository categoryRepository;

    private final PostRepository postRepository;
    private final PostCategoryRepository postCategoryRepository;
    private final PostImageService postImageService;    // 게시물 이미 관련 서비스

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
            throw new BusinessException(ErrorCode.POST_STATUS_REGISTER_FORBIDDEN);
        }

        // 완료/미완료/인계
        post.setStatus(dto.getStatus());

        // 공지사항인 경우
        if (PostType.NOTICE.equals(dto.getType())
                && !Role.ADMIN.equals(member.getRole())) {

            throw new BusinessException(ErrorCode.NOTICE_REGISTER_FORBIDDEN);
        }

        // 습득 게시물인 경우
        if (PostType.FIND.equals(dto.getType())) {
            // 분실물 발견 위치
            Location location = locationRepository.findById(dto.getLocationId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.LOCATION_NOT_FOUND));

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

        // 분실물(isPersonal=true)인 경우, 해당 학생에게 이메일 발송 (트랜잭션 커밋 후 발송)
        if (Boolean.TRUE.equals(post.getIsPersonal())) {

            Member targetStudent = memberRepository.findByStudentId(Long.valueOf(post.getStudentId()))
                    .orElseThrow(() -> new BusinessException(ErrorCode.POST_STUDENT_NOT_FOUND));

                eventPublisher.publishEvent(new PersonalLostMailEvent(
                        targetStudent.getEmail(),
                        targetStudent.getName(),
                        post.getTitle(),
                        post.getCreatedAt()
                ));
        }

        return post.getId();
    }

    @Override
    public Long updatePost(Long postId, PostDTO.UpdateContentRequest dto, CustomUserDetails userDetails) {

        Member member = userDetails.getMember();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        // 해당 게시물의 작성자도 아니고, 관리자도 아닌 경우
        if (!post.getMember().equals(member) &&
                !Role.ADMIN.equals(member.getRole())) {
            throw new BusinessException(ErrorCode.POST_UPDATE_FORBIDDEN);
        }

        if (dto.getLocationDetail() != null) post.setLocationDetail(dto.getLocationDetail());
        if (dto.getTitle() != null) post.setTitle(dto.getTitle());
        if (dto.getContent() != null) post.setContent(dto.getContent());
        if (dto.getStoredLocation() != null) post.setStoredLocation(dto.getStoredLocation());
        if (dto.getIsPersonal() != null) post.setIsPersonal(dto.getIsPersonal());

        // 발견 위치 수정
        if (dto.getLocationId() != null) {

            Location location = locationRepository.findById(dto.getLocationId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.LOCATION_NOT_FOUND));

            post.setLocation(location);
        }

        // 게시물 상태 수정
        if (dto.getStatus() != null) {

            // 일반 사용자는 인계 상태를 수정할 수 없음
            if (Role.USER.equals(member.getRole()) &&
                    PostStatus.POLICE.equals(dto.getStatus())) {
                throw new BusinessException(ErrorCode.POST_STATUS_UPDATE_FORBIDDEN);
            }

            post.setStatus(dto.getStatus());
        }

        // 게시물 유형 수정
        if (dto.getType() != null) {

            // 일반 사용자는 공지를 게시할 수 없음
            if (Role.USER.equals(member.getRole()) &&
                    PostType.NOTICE.equals(dto.getType())) {
                throw new BusinessException(ErrorCode.NOTICE_UPDATE_FORBIDDEN);
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
    public void updateStatusOfPosts(PostDTO.UpdateStatusRequest dto) {

        List<Post> posts = postRepository.findAllById(dto.getPostIds());

        for (Post p : posts) {

            if (dto.getStatus() != null) p.setStatus(dto.getStatus());
        }
    }

    @Override
    public void deletePost(Long postId, CustomUserDetails userDetails) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        if (!post.getMember().equals(userDetails.getMember()) &&
                !Role.ADMIN.equals(userDetails.getRole())) {
            throw new BusinessException(ErrorCode.POST_DELETE_FORBIDDEN);
        }

        // 게시물 이미지 삭제
        postImageService.deleteAllOf(post);
        // 게시물 삭제
        postRepository.delete(post);
    }

    @Override
    public void deletePosts(List<Long> postIds) {

        List<Post> posts = postRepository.findAllById(postIds);
        postImageService.deleteAllOf(posts);
        postRepository.deleteAll(posts);
    }

    @Override
    public PostDTO.Response getPostById(Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        List<String> categories = postCategoryRepository.findByPost(post).stream()
                .map(pc -> pc.getCategory().getName())
                .toList();

        List<String> images = postImageService.getUrls(post);

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
        // (게시물 id, List<카테고리명>)
        Map<Long, List<String>> allPostCategories = categories.stream()
                .collect(Collectors.groupingBy(
                        pc -> pc.getPost().getId(),
                        Collectors.mapping(
                                pc -> pc.getCategory().getName(), Collectors.toList()
                        )
                ));

        // (게시물 id, 이미지 url)
        Map<Long, List<String>> allPostImages = postImageService.getUrls(posts);

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

            eventPublisher.publishEvent(new LostItemMatchNotificationEvent(
                    lostMember.getId(),
                    title,
                    message,
                    link
            ));
        }

        log.info("[알림 전송 완료] 관련 분실자 수: {}", targetMembers.size());
    }
}
