package oneclass.oneclass.domain.announce.controller;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.announce.dto.request.CreateAnnounceRequest;
import oneclass.oneclass.domain.announce.dto.request.UpdateAnnounceRequest;
import oneclass.oneclass.domain.announce.dto.response.AnnounceResponse;
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
    private final AnnounceService announceService;

    @PostMapping("/create")
    public AnnounceResponse createAnnounce(@RequestBody CreateAnnounceRequest request) {
        return announceService.createAnnounce(request);
    }

    @GetMapping("/id/{id}")
    public AnnounceResponse findAnnounceById(@PathVariable Long id) {
        return announceService.findAnnounceById(id);
    }

    @GetMapping("/title/{title}")
    public AnnounceResponse findAnnounceByTitle(@PathVariable String title) {
        return announceService.findAnnounceByTitle(title);
    }

    @PatchMapping()
    public AnnounceResponse updateAnnounce(@RequestBody UpdateAnnounceRequest request) {
        return announceService.updateAnnounce(request);
    }

    @DeleteMapping("/{id}")
    public void deleteAnnounce(@PathVariable Long id) {
        announceService.deleteAnnounce(id);
    }

    @GetMapping("/all")
    public List<AnnounceResponse> findAll() {
        return announceService.findAll();
    }
}