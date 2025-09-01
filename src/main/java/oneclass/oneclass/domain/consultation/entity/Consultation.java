package oneclass.oneclass.domain.consultation.entity;

import jakarta.persistence.*;
import lombok.Data;
import oneclass.oneclass.domain.auth.member.entity.Member;

import java.time.LocalDateTime;

@Entity
@Data
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member student;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member parent;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member teacher;

    private LocalDateTime requestedAt;
    private LocalDateTime scheduledAt;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
