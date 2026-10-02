package NotFound.next_campus.domain.location.api;

import NotFound.next_campus.domain.location.dto.LocationDTO;
import NotFound.next_campus.domain.location.service.LocationService;
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

@Tag(name = "Location", description = "위치 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/locations")
public class LocationController {

    private final LocationService locationService;

    // 위치 생성
    @Operation(summary = "위치 생성", description = "새로운 위치를 등록합니다. ADMIN 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "위치 생성 성공"),
            @ApiResponse(responseCode = "403", description = "COMMON002 - 권한이 없습니다.")
    })
    @PostMapping
    public ResponseEntity<CommonResponse<LocationDTO.Response>> createLocation(
            @RequestBody LocationDTO.CreateRequest requestDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(CommonResponse.ok(locationService.createLocation(requestDTO, userDetails)));
    }

    // 전체 위치 조회
    @Operation(summary = "전체 위치 조회", description = "등록된 모든 위치 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseEntity<CommonResponse<List<LocationDTO.Response>>> getAllLocations() {

        return ResponseEntity.ok(CommonResponse.ok(locationService.getAllLocations()));
    }

    // 단일 위치 조회
    @Operation(summary = "단일 위치 조회", description = "위치 ID로 위치 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "LOCATION001 - 존재하지 않는 위치입니다.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<LocationDTO.Response>> getLocation(
            @Parameter(description = "위치 ID") @PathVariable Long id
    ) {

        return ResponseEntity.ok(CommonResponse.ok(locationService.getLocation(id)));
    }

    // 위치 수정
    @Operation(summary = "위치 수정", description = "위치 이름을 수정합니다. ADMIN 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "위치 수정 성공"),
            @ApiResponse(responseCode = "403", description = "COMMON002 - 권한이 없습니다."),
            @ApiResponse(responseCode = "404", description = "LOCATION001 - 존재하지 않는 위치입니다.")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<CommonResponse<LocationDTO.Response>> updateLocation(
            @Parameter(description = "위치 ID") @PathVariable Long id,
            @RequestBody LocationDTO.CreateRequest requestDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(CommonResponse.ok(locationService.updateLocation(id, requestDTO, userDetails)));
    }

    // 위치 삭제
    @Operation(summary = "위치 삭제", description = "위치를 삭제합니다. ADMIN 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "위치 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "COMMON002 - 권한이 없습니다.")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteLocation(
            @Parameter(description = "위치 ID") @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        locationService.deleteLocation(id, userDetails);

        return ResponseEntity.ok(CommonResponse.ok());
    }
}
