package oneclass.oneclass.domain.counsel.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank(message = "제목을 입력해주세요.")
    private String title;

    private String name;
    private String phone;
    private String parentPhone;

    private LocalDateTime date;
    private String type;
    private String subject;
    private String description;

    @Min(value = 0, message = "나이는 0 이상이어야 합니다.")
    private int age;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Gender gender;

    private LocalDateTime createAt;


    @PrePersist
    void onCreate() {
        if (createAt == null) createAt = LocalDateTime.now();
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