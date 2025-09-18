package oneclass.oneclass.domain.counsel.service;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.counsel.dto.ConsultationDetailResponse;
import oneclass.oneclass.domain.counsel.dto.ConsultationRequest;
import oneclass.oneclass.domain.counsel.entity.Consultation;
import oneclass.oneclass.domain.counsel.repository.ConsultationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultationService {
    private final ConsultationRepository consultationRepository;

    //상담신청(학생)
    public Consultation createConsultation(ConsultationRequest request){
        Consultation con = new Consultation();
        con.setName(request.getName());//학생이름
        con.setPhone(request.getPhone());//학생 전화번호
        con.setParentPhone(request.getParentPhone());//학부모 전화번호
        con.setDate(request.getDate());//희망하는 날짜
        con.setType(request.getType());
        con.setSubject(request.getSubject());
        con.setDescription(request.getDescription());
        con.setStatus("REQUESTED");//상담 신청이 완료되었다 라는 것을 보여주기 위함(확정이 아님)
        con.setCreateAt(LocalDateTime.now());
        return consultationRepository.save(con);
    }


    public ConsultationDetailResponse getConsultationDetail(String name, String phone) {
        Consultation consultation = consultationRepository.findByNameAndPhone(name, phone)
                .orElseThrow(() -> new IllegalArgumentException("상담 내역이 없습니다."));
        return ConsultationDetailResponse.from(consultation);
    }

    //전체조회
    public List<Consultation> getAllSchedule(){
        return consultationRepository.findAll();
    }
}