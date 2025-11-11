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

import java.time.LocalDateTime;
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
        con.setName(request.name());//학생이름
        con.setPhone(request.phone());//학생 전화번호
        con.setParentPhone(request.parentPhone());//학부모 전화번호
        con.setDate(request.date());//희망하는 날짜 ex) 2025-05-23 15:45
        con.setType(request.type());
        con.setSubject(request.subject());
        con.setDescription(request.description());
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

    // 상태 변경 및 부가 필드 업데이트 - 데이터 갱신이므로 트랜잭션
    @Transactional
    public Consultation changeStatus(ChangeConsultationStatusRequest request) {
        Consultation consultation = resolveTarget(request);

        // null 상태는 REQUESTED로 보정
        ConsultationStatus current = consultation.getStatus() != null
                ? consultation.getStatus()
                : ConsultationStatus.REQUESTED;

        ConsultationStatus target = request.status();

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
        if (request.consultationId() != null) {
            return consultationRepository.findById(request.consultationId())
                    .orElseThrow(() -> new CustomException(CounselError.NOT_FOUND));
        }

        // name+phone fallback (둘 중 하나라도 없으면 400)
        if (request.name() == null || request.phone() == null) {
            throw new CustomException(CounselError.BAD_REQUEST);
        }

        // 한 번의 쿼리로 결과를 가져와서 개수에 따라 분기 처리
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