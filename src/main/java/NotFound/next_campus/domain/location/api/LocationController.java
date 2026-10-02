package NotFound.next_campus.domain.location.api;

import NotFound.next_campus.domain.location.dto.LocationDTO;
import NotFound.next_campus.domain.location.service.LocationService;
import NotFound.next_campus.global.auth.user.CustomUserDetails;
import NotFound.next_campus.global.common.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/locations")
public class LocationController {

    private final LocationService locationService;

    // 위치 생성
    @PostMapping
    public ResponseEntity<CommonResponse<LocationDTO.Response>> createLocation(
            @RequestBody LocationDTO.CreateRequest requestDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(CommonResponse.ok(locationService.createLocation(requestDTO, userDetails)));
    }

    // 전체 위치 조회
    @GetMapping
    public ResponseEntity<CommonResponse<List<LocationDTO.Response>>> getAllLocations() {

        return ResponseEntity.ok(CommonResponse.ok(locationService.getAllLocations()));
    }

    // 단일 위치 조회
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<LocationDTO.Response>> getLocation(@PathVariable Long id) {

        return ResponseEntity.ok(CommonResponse.ok(locationService.getLocation(id)));
    }

    // 위치 수정
    @PatchMapping("/{id}")
    public ResponseEntity<CommonResponse<LocationDTO.Response>> updateLocation(
            @PathVariable Long id,
            @RequestBody LocationDTO.CreateRequest requestDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(CommonResponse.ok(locationService.updateLocation(id, requestDTO, userDetails)));
    }

    // 위치 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteLocation(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        locationService.deleteLocation(id, userDetails);

        return ResponseEntity.ok(CommonResponse.ok());
    }
}
