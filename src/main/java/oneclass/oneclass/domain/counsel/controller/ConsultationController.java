package oneclass.oneclass.domain.counsel.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.counsel.dto.ConsultationDetailResponse;
import oneclass.oneclass.domain.counsel.dto.ConsultationRequest;
import oneclass.oneclass.domain.counsel.entity.Consultation;
import oneclass.oneclass.domain.counsel.service.ConsultationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/consultations")
@RequiredArgsConstructor
public class ConsultationController {
    private final ConsultationService consultationService;

    //상담신청(학생)
    @PostMapping("/request")
    public ResponseEntity<Consultation> create(@RequestBody @Valid ConsultationRequest request){
        return ResponseEntity.ok(consultationService.createConsultation(request));
    }
    //상담 상태 변경 ex) 상담신청이 됨 -> 상담신청 날짜 확정
    @PostMapping("/change-status")
    public ResponseEntity<Consultation> changeStatus(@RequestBody @Valid ConsultationRequest request){
        return ResponseEntity.ok(consultationService.changeStatus(request));
    }

    //상담 상세 조회
    @GetMapping("/detail")
    public ResponseEntity<ConsultationDetailResponse> getConsultationDetail(
            @RequestParam String name,
            @RequestParam String phone) {
        ConsultationDetailResponse response = consultationService.getConsultationDetail(name, phone);
        return ResponseEntity.ok(response);
    }

    //전체상담조회
    @GetMapping("/schedule")
    public ResponseEntity<List<Consultation>> getAllSchedule(){
        return ResponseEntity.ok(consultationService.getAllSchedule());
    }
}