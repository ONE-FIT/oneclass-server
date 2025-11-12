package oneclass.oneclass.domain.announce.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.announce.dto.request.CreateAnnounceRequest;
import oneclass.oneclass.domain.announce.dto.request.UpdateAnnounceRequest;
import oneclass.oneclass.domain.announce.dto.response.AnnounceResponse;
import oneclass.oneclass.domain.announce.service.AnnounceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @Operation(summary = "강의에 공지 생성",
            description = "강의를 고르고 공지를 만듭니다.")
    @PostMapping("/lesson/{lessonId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnnounceResponse> createLessonAnnounce(
            @PathVariable Long lessonId,
            @RequestBody CreateAnnounceRequest request
    ) {
        return ResponseEntity.ok(announceService.createAnnounceForLesson(request, lessonId));
    }

    @Operation(summary = "검색",
            description = "반 전용 공지를 id로 찾습니다")
    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<List<AnnounceResponse>> getLessonAnnounces(@PathVariable Long lessonId) {
        return ResponseEntity.ok(announceService.findAnnouncesByLessonId(lessonId));
    }

    @Operation(summary = "학생 전용 공지 생성", description = "특정 학생에게만 공지를 생성합니다.")
    @PostMapping("/member/{memberId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnnounceResponse> createAnnounceForMember(
            @PathVariable Long memberId,
            @RequestBody CreateAnnounceRequest request
    ) {
        return ResponseEntity.ok(announceService.createAnnounceForMember(request, memberId));
    }

    @Operation(summary = "학생 공지 조회", description = "특정 학생에게 전달된 공지를 조회합니다.")
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<AnnounceResponse>> getAnnouncesByMemberId(@PathVariable Long memberId) {
        return ResponseEntity.ok(announceService.findAnnouncesByMemberId(memberId));
    }

    @Operation(summary = "검색",
            description = "공지를 id로 찾습니다")
    @GetMapping("/id/{id}")
    public AnnounceResponse findAnnounceById(@PathVariable Long id) {
        return announceService.findAnnounceById(id);
    }

    @Operation(summary = "검색",
            description = "제목으로 공지를 검색합니다 (동일 제목 여러 개 가능)")
    @GetMapping("/title/{title}")
    public List<AnnounceResponse> findAnnounceByTitle(@PathVariable String title) {
        return announceService.findAnnouncesByTitle(title);
    }

    @Operation(summary = "공지 수정",
            description = "공지를 수정합니다")
    @PatchMapping()
    public AnnounceResponse updateAnnounce(@RequestBody UpdateAnnounceRequest request) {
        return announceService.updateAnnounce(request);
    }

    @Operation(summary = "공지 삭제",
            description = "공지를 삭제합니다")
    @DeleteMapping("/{id}")
    public void deleteAnnounce(@PathVariable Long id) {
        announceService.deleteAnnounce(id);
    }

    @Operation(summary = "검색",
            description = "공지를 모두 찾습니다")
    @GetMapping("/all")
    public List<AnnounceResponse> findAll() {
        return announceService.findAll();
    }
}