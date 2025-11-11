package oneclass.oneclass.domain.counsel.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
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

    private String age;
    private String gender;

    @Enumerated(EnumType.STRING)

    private LocalDateTime createAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConsultationStatus status;

    @PrePersist
    void onCreate() {
        if (createAt == null) createAt = LocalDateTime.now();
        if (status == null) status = ConsultationStatus.REQUESTED;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Consultation that)) return false;
        return id != null && java.util.Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }
}