package oneclass.oneclass.domain.announce.entity;

import jakarta.persistence.*;
import lombok.Data;
import oneclass.oneclass.global.auth.member.entity.Member;

@Entity
@Data
public class Announce {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member author;
}
