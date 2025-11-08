package oneclass.oneclass.domain.counsel.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.counsel.dto.request.ChangeConsultationStatusRequest;
import oneclass.oneclass.domain.counsel.dto.response.ConsultationDetailResponse;
import oneclass.oneclass.domain.counsel.dto.request.ConsultationRequest;
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
    @Operation(summary = "상담 신청", description = "상담을 신청합니다.")
    @PostMapping("/request")
    public ResponseEntity<Consultation> create(@RequestBody @Valid ConsultationRequest request) {
        return ResponseEntity.ok(consultationService.createConsultation(request));
    }

    //상담 상태 변경 ex) 상담신청이 됨 -> 상담신청 날짜 확정
    @Operation(summary = "상담 날짜 확정", description = "상담 날짜를 확정합니다.")
    @PostMapping("/change-status")
    public ResponseEntity<Consultation> changeStatus(@RequestBody @Valid ChangeConsultationStatusRequest request) {
        return ResponseEntity.ok(consultationService.changeStatus(request));
    }

    //상담 상세 조회
    @Operation(summary = "상담 상세조회", description = "상담을 상세조회합니다.")
    @GetMapping("/detail")
    public ResponseEntity<ConsultationDetailResponse> getConsultationDetail(
            @RequestParam String name,
            @RequestParam String phone) {
        ConsultationDetailResponse response = consultationService.getConsultationDetail(name, phone);
        return ResponseEntity.ok(response);
    }

    //전체상담조회
    @Operation(summary = "전체 상담조회", description = "전체상담을 조회합니다.")
    @GetMapping("/schedule")
    public ResponseEntity<List<Consultation>> getAllSchedule() {
        return ResponseEntity.ok(consultationService.getAllSchedule());
    }
}