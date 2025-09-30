package oneclass.oneclass.domain.academy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "academy_refresh_token",
        indexes = {
                @Index(name = "idx_academy_refresh_token_code", columnList = "academyCode")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_academy_refresh_token_academy_code", columnNames = "academyCode")
        }
)
public class AcademyRefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String academyCode;    // 학원코드 (Unique)

    @Column(nullable = false, length = 300)
    private String token;          // Refresh Token 문자열

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    public boolean isExpired() {
        return expiryDate.isBefore(LocalDateTime.now());
    }

    public void rotate(String newToken, LocalDateTime newExpiry) {
        this.token = newToken;
        this.expiryDate = newExpiry;
    }
}