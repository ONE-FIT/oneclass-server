package oneclass.oneclass.domain.member.entity;

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
public class RefreshToken {

    // phone을 주키로 사용 (전화번호 로그인 기준)
    @Id
    @Column(nullable = false, unique = true)
    private String phone;

    // 토큰 문자열
    @Column(nullable = false, length = 300)
    private String token;

    // 만료 시각
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