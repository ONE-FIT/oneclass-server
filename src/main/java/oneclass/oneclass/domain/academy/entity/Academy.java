package oneclass.oneclass.domain.academy.entity;

import jakarta.persistence.*;
import lombok.*;
import oneclass.oneclass.domain.member.entity.Member;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 프록시용
@Getter
@Entity
@Setter
@Builder
public class Academy {
    @Id
    private String academyCode;

    @Column(nullable = false)
    private String academyName;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true, length = 11)
    private String phone;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "academy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Member> members = new ArrayList<>();

    // ===== approval fields =====
    public enum Status {
        PENDING,
        APPROVED,
        REJECTED
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING; // 기본은 PENDING

    @Column(name = "approved_by")
    private String approvedBy; // admin username

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    // 기존 빌더(카피 생성자 등) 필요 시 사용
    @Builder
    public Academy(String academyCode, String academyName, String password, String email, String phone, Role role, List<Member> members, Status status, String approvedBy, LocalDateTime approvedAt) {
        this.academyCode = academyCode;
        this.academyName = academyName;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.role = role;
        if (members != null) this.members = members;
        this.status = status != null ? status : Status.PENDING;
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
    }

    // 편의 메서드: 승인 처리
    public void approve(String adminUsername) {
        this.status = Status.APPROVED;
        this.approvedBy = adminUsername;
        this.approvedAt = LocalDateTime.now();
    }

    public void reject(String adminUsername) {
        this.status = Status.REJECTED;
        this.approvedBy = adminUsername;
        this.approvedAt = LocalDateTime.now();
    }
}