package oneclass.oneclass.domain.announce.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.announce.dto.request.CreateAnnounceRequest;
import oneclass.oneclass.domain.announce.dto.request.UpdateAnnounceRequest;
import oneclass.oneclass.domain.announce.dto.response.AnnounceResponse;
import oneclass.oneclass.domain.announce.service.AnnounceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/announce")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AnnounceController {
    private final AnnounceService announceService;

    @Operation(summary = "공지 생성",
            description = "공지를 만듭니다.")
    @PostMapping("/create")
    public AnnounceResponse createAnnounce(@RequestBody CreateAnnounceRequest request) {
        return announceService.createAnnounce(request);
    }

    @Operation(summary = "검색",
            description = "강의를 id로 찾습니다")
    @GetMapping("/id/{id}")
    public AnnounceResponse findAnnounceById(@PathVariable Long id) {
        return announceService.findAnnounceById(id);
    }

    @Operation(summary = "검색",
            description = "강의를 제목로 찾습니다")
    @GetMapping("/title/{title}")
    public AnnounceResponse findAnnounceByTitle(@PathVariable String title) {
        return announceService.findAnnounceByTitle(title);
    }

    @Operation(summary = "강의 수정",
            description = "강의를 수정합니다")
    @PatchMapping()
    public AnnounceResponse updateAnnounce(@RequestBody UpdateAnnounceRequest request) {
        return announceService.updateAnnounce(request);
    }

    @Operation(summary = "강의 삭제",
            description = "강의를 삭제합니다")
    @DeleteMapping("/{id}")
    public void deleteAnnounce(@PathVariable Long id) {
        announceService.deleteAnnounce(id);
    }

    @Operation(summary = "검색",
            description = "강의를 모두 찾습니다")
    @GetMapping("/all")
    public List<AnnounceResponse> findAll() {
        return announceService.findAll();
    }
}