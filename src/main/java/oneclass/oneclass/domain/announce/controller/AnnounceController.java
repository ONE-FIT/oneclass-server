package oneclass.oneclass.domain.announce.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.announce.dto.request.CreateAnnounceRequest;
import oneclass.oneclass.domain.announce.dto.request.UpdateAnnounceRequest;
import oneclass.oneclass.domain.announce.dto.response.AnnounceResponse;
import oneclass.oneclass.domain.announce.service.AnnounceService;
import oneclass.oneclass.global.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/announce")
@RequiredArgsConstructor
public class AnnounceController {
    private final AnnounceService announceService;

    @Operation(summary = "공지 생성",
            description = "공지를 만듭니다.")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<AnnounceResponse>> createAnnounce(@RequestBody @Valid CreateAnnounceRequest request) {
        AnnounceResponse response = announceService.createAnnounce(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "검색",
            description = "강의를 id로 찾습니다")
    @GetMapping("/id/{id}")
    public ResponseEntity<ApiResponse<AnnounceResponse>> findAnnounceById(@PathVariable Long id) {
        AnnounceResponse response = announceService.findAnnounceById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "검색",
            description = "강의를 제목로 찾습니다")
    @GetMapping("/title/{title}")
    public ResponseEntity<ApiResponse<AnnounceResponse>> findAnnounceByTitle(@PathVariable String title) {
        AnnounceResponse response = announceService.findAnnounceByTitle(title);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "강의 수정",
            description = "강의를 수정합니다")
    @PatchMapping()
    public ResponseEntity<ApiResponse<AnnounceResponse>> updateAnnounce(@RequestBody @Valid UpdateAnnounceRequest request) {
        AnnounceResponse response = announceService.updateAnnounce(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "강의 삭제",
            description = "강의를 삭제합니다")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAnnounce(@PathVariable Long id) {
        announceService.deleteAnnounce(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "검색",
            description = "강의를 모두 찾습니다")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<AnnounceResponse>>> findAll() {
        List<AnnounceResponse> responses = announceService.findAll();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}