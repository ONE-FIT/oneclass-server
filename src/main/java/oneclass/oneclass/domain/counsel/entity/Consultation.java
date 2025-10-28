package oneclass.oneclass.domain.counsel.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Consultation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phone;
    private String parentPhone;

    private LocalDateTime date;
    private String type;
    private String subject;
    private String description;

    private LocalDateTime createAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConsultationStatus status;

    @PrePersist
    void onCreate() {
        if (createAt == null) createAt = LocalDateTime.now();
        if (status == null) status = ConsultationStatus.REQUESTED;
    }
}