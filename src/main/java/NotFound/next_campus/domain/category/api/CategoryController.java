package NotFound.next_campus.domain.category.api;

import NotFound.next_campus.domain.category.dto.CategoryDTO;
import NotFound.next_campus.domain.category.service.CategoryService;
import NotFound.next_campus.global.auth.user.CustomUserDetails;
import NotFound.next_campus.global.common.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    // 카테고리 생성
    @PostMapping
    public ResponseEntity<CommonResponse<CategoryDTO.Response>> createCategory(
            @RequestBody CategoryDTO.CreateRequest requestDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        CategoryDTO.Response response = categoryService.createCategory(requestDTO, userDetails);

        return ResponseEntity.ok(CommonResponse.ok(response));
    }

    // 전체 카테고리 조회
    @GetMapping
    public ResponseEntity<CommonResponse<List<CategoryDTO.Response>>> getAllCategories() {
        return ResponseEntity.ok(CommonResponse.ok(categoryService.getAllCategories()));
    }

    // 단일 카테고리 조회
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<CategoryDTO.Response>> getCategory(@PathVariable Long id) {

        return ResponseEntity.ok(CommonResponse.ok(categoryService.getCategory(id)));
    }

    // 카테고리 수정
    @PatchMapping("/{id}")
    public ResponseEntity<CommonResponse<CategoryDTO.Response>> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryDTO.CreateRequest requestDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(CommonResponse.ok(categoryService.updateCategory(id, requestDTO, userDetails)));
    }

    // 카테고리 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteCategory(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        categoryService.deleteCategory(id, userDetails);

        return ResponseEntity.ok(CommonResponse.ok());
    }
}
