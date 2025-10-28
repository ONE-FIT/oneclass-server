package oneclass.oneclass.domain.counsel.service;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.counsel.dto.ChangeConsultationStatusRequest;
import oneclass.oneclass.domain.counsel.dto.ConsultationDetailResponse;
import oneclass.oneclass.domain.counsel.dto.ConsultationRequest;
import oneclass.oneclass.domain.counsel.entity.Consultation;
import oneclass.oneclass.domain.counsel.entity.ConsultationStatus;
import oneclass.oneclass.domain.counsel.error.CounselError;
import oneclass.oneclass.domain.counsel.repository.ConsultationRepository;
import oneclass.oneclass.global.exception.CustomException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ConsultationService {
    private final ConsultationRepository consultationRepository;

    //상담신청
    public Consultation createConsultation(ConsultationRequest request) {
        Consultation con = new Consultation();
        con.setName(request.getName());//학생이름
        con.setPhone(request.getPhone());//학생 전화번호
        con.setParentPhone(request.getParentPhone());//학부모 전화번호
        con.setDate(request.getDate());//희망하는 날짜
        con.setType(request.getType());
        con.setSubject(request.getSubject());
        con.setDescription(request.getDescription());
        con.setStatus(ConsultationStatus.REQUESTED);//상담 신청이 완료되었다 라는 것을 보여주기 위함(확정이 아님)
        con.setCreateAt(LocalDateTime.now());
        return consultationRepository.save(con);
    }

    // 상태 전이 규칙
    private static final Map<ConsultationStatus, Set<ConsultationStatus>> ALLOWED = new EnumMap<>(ConsultationStatus.class);
    static {
        ALLOWED.put(ConsultationStatus.REQUESTED, Set.of(ConsultationStatus.CONFIRMED, ConsultationStatus.CANCELLED));
        ALLOWED.put(ConsultationStatus.CONFIRMED, Set.of(ConsultationStatus.COMPLETED, ConsultationStatus.CANCELLED));
        ALLOWED.put(ConsultationStatus.CANCELLED, Set.of());
        ALLOWED.put(ConsultationStatus.COMPLETED, Set.of());
    }

    public Consultation changeStatus(ChangeConsultationStatusRequest request) {
        Consultation consultation = resolveTarget(request);

        // null 상태는 REQUESTED로 보정
        ConsultationStatus current = consultation.getStatus() != null
                ? consultation.getStatus()
                : ConsultationStatus.REQUESTED;

        ConsultationStatus target = request.getStatus();

        // 상태 입력 없이 보조 필드만 갱신
        if (target == null) {
            applyOptionalUpdates(consultation, request);
            return consultationRepository.save(consultation);
        }

        // 같은 상태라면 보조 필드만 갱신
        if (current == target) {
            applyOptionalUpdates(consultation, request);
            return consultationRepository.save(consultation);
        }

        // 전이 가능 여부 검증
        Set<ConsultationStatus> allowedNext = ALLOWED.getOrDefault(current, Set.of());
        if (!allowedNext.contains(target)) {
            throw new CustomException(CounselError.BAD_REQUEST, "상태 전이가 허용되지 않습니다: " + current + " -> " + target);
        }

        // 상태 및 기타 필드 갱신
        consultation.setStatus(target);
        applyOptionalUpdates(consultation, request);

        return consultationRepository.save(consultation);
    }

    private Consultation resolveTarget(ChangeConsultationStatusRequest request) {
        // ID가 오면 ID 우선
        if (request.getConsultationId() != null) {
            return consultationRepository.findById(request.getConsultationId())
                    .orElseThrow(() -> new CustomException(CounselError.NOT_FOUND));
        }

        // name+phone fallback (둘 중 하나라도 없으면 400)
        if (request.getName() == null || request.getPhone() == null) {
            throw new CustomException(CounselError.BAD_REQUEST);
        }

        // 한 번의 쿼리로 결과를 가져와서 개수에 따라 분기 처리
        List<Consultation> matches = consultationRepository.findByNameAndPhone(request.getName(), request.getPhone());

        if (matches.isEmpty()) {
            throw new CustomException(CounselError.NOT_FOUND);
        }
        if (matches.size() > 1) {
            throw new CustomException(CounselError.CONFLICT);
        }

        return matches.get(0);
    }


    private void applyOptionalUpdates(Consultation consultation, ChangeConsultationStatusRequest request) {
        if (request.getDate() != null) {
            consultation.setDate(request.getDate());
        }
        if (request.getSubject() != null) {
            consultation.setSubject(request.getSubject());
        }
        if (request.getDescription() != null) {
            consultation.setDescription(request.getDescription());
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

    //전체조회
    public List<Consultation> getAllSchedule() {
        return consultationRepository.findAll();
    }
}