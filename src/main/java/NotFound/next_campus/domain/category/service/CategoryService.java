package NotFound.next_campus.domain.category.service;

import NotFound.next_campus.domain.category.dto.CategoryDTO;
import NotFound.next_campus.domain.category.model.Category;
import NotFound.next_campus.domain.category.repository.CategoryRepository;
import NotFound.next_campus.domain.member.model.Role;
import NotFound.next_campus.global.auth.user.CustomUserDetails;
import NotFound.next_campus.global.exception.BusinessException;
import NotFound.next_campus.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // 새로운 카테고리 생성
    public CategoryDTO.Response createCategory(CategoryDTO.CreateRequest requestDTO, CustomUserDetails userDetails) {

        // UserDetails에서 role 추출
        Role role = userDetails.getMember().getRole();

        if (role != Role.ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        //현재 로그인한 사용자의 이름과 role 로그로 확인
        System.out.println("현재 사용자: " + userDetails.getUsername());
        System.out.println("현재 권한들: " + userDetails.getAuthorities());

        // 카테고리 생성
        Category category = Category.builder()
                .name(requestDTO.getName())
                .build();

        Category saved = categoryRepository.save(category);

        return new CategoryDTO.Response(saved.getId(), saved.getName());
    }

    //모든 카테고리 조회
    public List<CategoryDTO.Response> getAllCategories() {

        return categoryRepository.findAll().stream()
                .map(c -> new CategoryDTO.Response(c.getId(), c.getName()))
                .collect(Collectors.toList());
    }

    //특정 카테고리 조회
    public CategoryDTO.Response getCategory(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        return new CategoryDTO.Response(category.getId(), category.getName());
    }

    //카테고리 수정
    public CategoryDTO.Response updateCategory(Long id, CategoryDTO.CreateRequest requestDTO, CustomUserDetails userDetails) {

        // 권한 체크
        if (!userDetails.getMember().getRole().equals(Role.ADMIN)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        category.setName(requestDTO.getName());
        Category updated = categoryRepository.save(category);

        return new CategoryDTO.Response(updated.getId(), updated.getName());
    }

    //카테고리 삭제
    public void deleteCategory(Long id, CustomUserDetails userDetails) {

        // 권한 체크
        if (!userDetails.getMember().getRole().equals(Role.ADMIN)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        categoryRepository.deleteById(id);
    }
}