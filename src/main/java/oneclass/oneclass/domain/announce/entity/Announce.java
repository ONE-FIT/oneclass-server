package oneclass.oneclass.domain.announce.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import oneclass.oneclass.global.auth.member.entity.Member;

@Entity
@Data
@NoArgsConstructor
@SuperBuilder
@Table(name = "Announce")
public class Announce {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Boolean important;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member author;
}