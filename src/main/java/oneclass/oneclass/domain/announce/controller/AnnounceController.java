package oneclass.oneclass.domain.announce.controller;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.announce.dto.request.CreateAnnounceRequest;
import oneclass.oneclass.domain.announce.dto.request.UpdateAnnounceRequest;
import oneclass.oneclass.domain.announce.dto.response.AnnounceResponse;
import oneclass.oneclass.domain.announce.repository.AnnounceRepository;
import oneclass.oneclass.domain.announce.service.AnnounceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/announce")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AnnounceController {
    private final AnnounceRepository announceRepository;
    private final AnnounceService announceService;

    @PostMapping("/create")
    public AnnounceResponse createAnnounce(@RequestBody CreateAnnounceRequest request) {
        ResponseEntity.ok(Map.of("message", "공지가 생성되었습니다."));
        return announceService.createAnnounce(request);
    }

    @GetMapping("/id/{id}")
    public AnnounceResponse findAnnounceById(@RequestParam @PathVariable Long id) {
        ResponseEntity.ok(Map.of("message", "공지가 제공되었습니다."));
        return announceService.findAnnounceById(id);
    }

    @GetMapping("/title/{title}")
    public AnnounceResponse findAnnounceByTitle(@RequestParam @PathVariable String title) {
        ResponseEntity.ok(Map.of("message", "공지가 제공되었습니다."));
        return announceService.findAnnounceByTitle(title);
    }

    @PatchMapping()
    public AnnounceResponse updateAnnounce(@RequestBody UpdateAnnounceRequest request) {
        ResponseEntity.ok(Map.of("message", "과제가 수정되었습니다."));
        return announceService.updateAnnounce(request);
    }

    @DeleteMapping("/{id}")
    public void deleteAnnounce(@PathVariable Long id) {
        ResponseEntity.ok(Map.of("message", "공지가 삭제되었습니다."));
        announceService.deleteAnnounce(id);
    }

    @GetMapping("/all")
    public List<AnnounceResponse> findAll() {
        return announceService.findAll();
    }
}
