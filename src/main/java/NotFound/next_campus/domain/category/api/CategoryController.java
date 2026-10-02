package NotFound.next_campus.domain.category.api;

import NotFound.next_campus.domain.category.dto.CategoryDTO;
import NotFound.next_campus.domain.category.service.CategoryService;
import NotFound.next_campus.global.auth.user.CustomUserDetails;
import NotFound.next_campus.global.common.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Category", description = "카테고리 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    // 카테고리 생성
    @Operation(summary = "카테고리 생성", description = "새로운 카테고리를 생성합니다. ADMIN 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "카테고리 생성 성공"),
            @ApiResponse(responseCode = "403", description = "COMMON002 - 권한이 없습니다.")
    })
    @PostMapping
    public ResponseEntity<CommonResponse<CategoryDTO.Response>> createCategory(
            @RequestBody CategoryDTO.CreateRequest requestDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        CategoryDTO.Response response = categoryService.createCategory(requestDTO, userDetails);

        return ResponseEntity.ok(CommonResponse.ok(response));
    }

    // 전체 카테고리 조회
    @Operation(summary = "전체 카테고리 조회", description = "등록된 모든 카테고리 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseEntity<CommonResponse<List<CategoryDTO.Response>>> getAllCategories() {
        return ResponseEntity.ok(CommonResponse.ok(categoryService.getAllCategories()));
    }

    // 단일 카테고리 조회
    @Operation(summary = "단일 카테고리 조회", description = "카테고리 ID로 카테고리 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "CATEGORY001 - 존재하지 않는 카테고리입니다.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<CategoryDTO.Response>> getCategory(
            @Parameter(description = "카테고리 ID") @PathVariable Long id
    ) {

        return ResponseEntity.ok(CommonResponse.ok(categoryService.getCategory(id)));
    }

    // 카테고리 수정
    @Operation(summary = "카테고리 수정", description = "카테고리 이름을 수정합니다. ADMIN 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "카테고리 수정 성공"),
            @ApiResponse(responseCode = "403", description = "COMMON002 - 권한이 없습니다."),
            @ApiResponse(responseCode = "404", description = "CATEGORY001 - 존재하지 않는 카테고리입니다.")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<CommonResponse<CategoryDTO.Response>> updateCategory(
            @Parameter(description = "카테고리 ID") @PathVariable Long id,
            @RequestBody CategoryDTO.CreateRequest requestDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(CommonResponse.ok(categoryService.updateCategory(id, requestDTO, userDetails)));
    }

    // 카테고리 삭제
    @Operation(summary = "카테고리 삭제", description = "카테고리를 삭제합니다. ADMIN 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "카테고리 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "COMMON002 - 권한이 없습니다.")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteCategory(
            @Parameter(description = "카테고리 ID") @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        categoryService.deleteCategory(id, userDetails);

        return ResponseEntity.ok(CommonResponse.ok());
    }
}
