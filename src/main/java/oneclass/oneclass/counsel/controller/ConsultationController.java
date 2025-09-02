package oneclass.oneclass.counsel.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.counsel.dto.ConsultationDetailResponse;
import oneclass.oneclass.counsel.dto.ConsultationRequest;
import oneclass.oneclass.counsel.entity.Consultation;
import oneclass.oneclass.counsel.service.ConsultationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultations")
@RequiredArgsConstructor
public class ConsultationController {
    private final ConsultationService consultationService;

    //상담신청
    @PostMapping("/request")
    public ResponseEntity<Consultation> create(@RequestBody @Valid ConsultationRequest request){
        return ResponseEntity.ok(consultationService.createConsultation(request));
    }
    //상담 상세 조회
    @GetMapping("/detail")
    public ResponseEntity<ConsultationDetailResponse> getConsultationDetail(
            @RequestParam String name,
            @RequestParam String phone) {
        ConsultationDetailResponse response = consultationService.getConsultationDetail(name, phone);
        return ResponseEntity.ok(response);
    }
    //일단 필요없어 보여서 주석처리
//    //@@의 상담 대기중 -> 확정
//    @PatchMapping("/{id}/status")
//    public ResponseEntity<Consultation> updateStatus(
//            @PathVariable Long id,
//            @RequestBody Map<String, String> request) {
//
//        String status = request.get("status");
//        String scheduleTime = request.get("scheduleTime");
//        return ResponseEntity.ok(consultationService.updateStatus(id, status, scheduleTime));
//    }
    //전체상담조회
    @GetMapping("/schedule")
    public ResponseEntity<List<Consultation>> getAllSchedule(){
        return ResponseEntity.ok(consultationService.getAllSchedule());
    }
}