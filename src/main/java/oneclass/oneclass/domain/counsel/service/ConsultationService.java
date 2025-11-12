package oneclass.oneclass.domain.counsel.service;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.counsel.dto.request.ConsultationRequest;
import oneclass.oneclass.domain.counsel.dto.request.UpdateConsultationRequest;
import oneclass.oneclass.domain.counsel.dto.response.ConsultationDetailResponse;
import oneclass.oneclass.domain.counsel.entity.Consultation;
import oneclass.oneclass.domain.counsel.error.CounselError;
import oneclass.oneclass.domain.counsel.repository.ConsultationRepository;
import oneclass.oneclass.global.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultationService {
    private final ConsultationRepository consultationRepository;

    @Transactional
    public Consultation createConsultation(ConsultationRequest request) {
        Consultation con = new Consultation();
        con.setTitle(request.title());
        con.setName(request.name());
        con.setPhone(request.phone());
        con.setDate(request.date());
        con.setType(request.type());
        con.setSubject(request.subject());
        con.setDescription(request.description());
        con.setAge(request.age());
        con.setGender(request.gender());
        return consultationRepository.save(con);
    }

    @Transactional
    public Consultation updateConsultation(UpdateConsultationRequest request) {
        Consultation consultation = resolveTarget(request);
        applyOptionalUpdates(consultation, request);
        return consultationRepository.save(consultation);
    }

    private Consultation resolveTarget(UpdateConsultationRequest request) {
        if (request.consultationId() != null) {
            return consultationRepository.findById(request.consultationId())
                    .orElseThrow(() -> new CustomException(CounselError.NOT_FOUND));
        }
        if (request.name() == null || request.phone() == null) {
            throw new CustomException(CounselError.BAD_REQUEST, "상담자 이름과 전화번호가 필요합니다.");
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

    private void applyOptionalUpdates(Consultation consultation, UpdateConsultationRequest request) {
        if (request.date() == null && request.subject() == null && request.description() == null) {
            throw new CustomException(CounselError.BAD_REQUEST, "수정할 필드를 입력하세요.");
        }
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
            throw new CustomException(CounselError.BAD_REQUEST, "상담자 이름과 전화번호가 필요합니다.");
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

    public List<Consultation> getAllSchedule() {
        return consultationRepository.findAll();
    }
}