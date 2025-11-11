package oneclass.oneclass.domain.counsel.service;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.counsel.dto.request.ChangeConsultationStatusRequest;
import oneclass.oneclass.domain.counsel.dto.response.ConsultationDetailResponse;
import oneclass.oneclass.domain.counsel.dto.request.ConsultationRequest;
import oneclass.oneclass.domain.counsel.entity.Consultation;
import oneclass.oneclass.domain.counsel.entity.ConsultationStatus;
import oneclass.oneclass.domain.counsel.error.CounselError;
import oneclass.oneclass.domain.counsel.repository.ConsultationRepository;
import oneclass.oneclass.global.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ConsultationService {
    private final ConsultationRepository consultationRepository;

    // 상담신청 - 데이터 생성이므로 트랜잭션
    @Transactional
    public Consultation createConsultation(ConsultationRequest request) {
        Consultation con = new Consultation();
        con.setTitle(request.title());
        con.setName(request.name());//학생이름
        con.setParentPhone(request.parentPhone());//학부모 전화번호
        con.setDate(request.date());//희망하는 날짜 ex) 2025-05-23 15:45
        con.setType(request.type());
        con.setSubject(request.subject());
        con.setDescription(request.description());
        con.setAge(request.age());
        con.setGender(request.gender());
        return consultationRepository.save(con);
    }



    @Transactional
    public Consultation changeStatus(ChangeConsultationStatusRequest request) {
        Consultation consultation = resolveTarget(request);
        applyOptionalUpdates(consultation, request);
        return consultationRepository.save(consultation);
    }

    private Consultation resolveTarget(ChangeConsultationStatusRequest request) {
        // ID가 오면 ID 우선
        if (request.consultationId() != null) {
            return consultationRepository.findById(request.consultationId())
                    .orElseThrow(() -> new CustomException(CounselError.NOT_FOUND));
        }
        // name+phone fallback (둘 중 하나라도 없으면 400)
        if (request.name() == null || request.phone() == null) {
            throw new CustomException(CounselError.BAD_REQUEST);
        }
        List<Consultation> matches = consultationRepository.findByNameAndPhone(request.name(), request.phone());
        if (matches.isEmpty()) {
            throw new CustomException(CounselError.NOT_FOUND);
        }
        if (matches.size() > 1) {
            throw new CustomException(CounselError.CONFLICT);
        }
        return matches.get(0);
    }

    private void applyOptionalUpdates(Consultation consultation, ChangeConsultationStatusRequest request) {
        if (request.date() != null) {
            consultation.setDate(request.date());
        }
        if (request.subject() != null) {
            consultation.setSubject(request.subject());
        }
        if (request.description() != null) {
            consultation.setDescription(request.description());
        }
    }

    public ConsultationDetailResponse getConsultationDetail(String name, String phone) {
        if (name == null || phone == null) {
            throw new CustomException(CounselError.BAD_REQUEST);
        }

        List<Consultation> matches = consultationRepository.findByNameAndPhone(name, phone);

        if (matches.isEmpty()) {
            throw new CustomException(CounselError.NOT_FOUND);
        }
        if (matches.size() > 1) {
            throw new CustomException(CounselError.CONFLICT);
        }

        return ConsultationDetailResponse.from(matches.get(0));
    }

    // 전체 조회
    public List<Consultation> getAllSchedule() {
        return consultationRepository.findAll();
    }
}