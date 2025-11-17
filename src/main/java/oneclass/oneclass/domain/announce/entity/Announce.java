package oneclass.oneclass.domain.announce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "announce")
public class Announce {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;
    private Boolean important;

    // 반 대상 public/lesson 공지에 사용
    private Long lessonId;

    // 개별 학생 대상 공지에 사용 (null이면 대상 없음)
    private Long memberId;

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public void update(String title, String content, Boolean important) {
        this.title = title;
        this.content = content;
        this.important = important;
    }
}