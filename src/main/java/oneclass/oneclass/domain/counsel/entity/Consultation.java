package oneclass.oneclass.domain.counsel.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Consultation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phone;

    private String type;// 신규 or 재학생
    private String subject;//과목
    private String description;//상담 내용

    private String status;//상담 신청됨(대기중) or 상담 날짜 확정
    private LocalDateTime scheduleTime;

    private LocalDateTime createAt;
}