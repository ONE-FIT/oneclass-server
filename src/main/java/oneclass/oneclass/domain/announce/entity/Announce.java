package oneclass.oneclass.domain.announce.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import oneclass.oneclass.domain.member.entity.Member;

@Entity
@Getter
@Setter
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