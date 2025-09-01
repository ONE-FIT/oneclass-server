package oneclass.oneclass.domain.admin.announce.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
//import oneclass.oneclass.domain.auth.member.entity.Member;

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

//    @ManyToOne(fetch = FetchType.LAZY)
//    private Member author;
}