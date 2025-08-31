package oneclass.oneclass.counsel.service;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.counsel.dto.ConsultationRequest;
import oneclass.oneclass.counsel.entity.Consultation;
import oneclass.oneclass.counsel.repository.ConsultationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultationService {
    private final ConsultationRepository consultationRepository;

    //상담신청
    public Consultation createConsultation(ConsultationRequest request){
        Consultation con = new Consultation();
        con.setName(request.getName());
        con.setPhone(request.getPhone());
        con.setType(request.getType());
        con.setSubject(request.getSubject());
        con.setDescription(request.getDescription());
        con.setStatus("REQUESTED");
        con.setCreateAt(LocalDateTime.now());
        return consultationRepository.save(con);
    }
    //상담조회
    public Consultation updateStatus(Long id, String status , String scheduleTime) {
        Consultation con = consultationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상담을 찾을 수 없습니다."));
        con.setStatus(status);
        if (scheduleTime != null) {
            con.setScheduleTime(LocalDateTime.parse(scheduleTime));
        }
        return consultationRepository.save(con);
    }

    //전체조회
    public List<Consultation> getAllSchedule(){
        return consultationRepository.findByStatus("CONFIRMED");
    }
}