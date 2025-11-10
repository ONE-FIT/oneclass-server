package oneclass.oneclass.domain.announce.controller;

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

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<AnnounceResponse>> createAnnounce(@RequestBody @Valid CreateAnnounceRequest request) {
        AnnounceResponse response = announceService.createAnnounce(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<ApiResponse<AnnounceResponse>> findAnnounceById(@PathVariable Long id) {
        AnnounceResponse response = announceService.findAnnounceById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/title/{title}")
    public ResponseEntity<ApiResponse<AnnounceResponse>> findAnnounceByTitle(@PathVariable String title) {
        AnnounceResponse response = announceService.findAnnounceByTitle(title);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping()
    public ResponseEntity<ApiResponse<AnnounceResponse>> updateAnnounce(@RequestBody @Valid UpdateAnnounceRequest request) {
        AnnounceResponse response = announceService.updateAnnounce(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAnnounce(@PathVariable Long id) {
        announceService.deleteAnnounce(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<AnnounceResponse>>> findAll() {
        List<AnnounceResponse> responses = announceService.findAll();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}