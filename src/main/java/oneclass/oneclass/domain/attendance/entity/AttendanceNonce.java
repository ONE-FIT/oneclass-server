package oneclass.oneclass.domain.attendance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "attendance_nonce")
public class AttendanceNonce {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long lessonId;

    private String nonce;  // UUID

    private LocalDateTime createdAt;

    private LocalDateTime expireAt;

    private boolean used;  // 이미 사용되었는지
}