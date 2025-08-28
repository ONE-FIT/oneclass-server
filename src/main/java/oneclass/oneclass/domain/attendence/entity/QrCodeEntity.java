package oneclass.oneclass.domain.attendence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import oneclass.oneclass.global.common.entity.BaseTimeEntity;


@Entity
@Getter
@SuperBuilder
@Table(name = "tb_check_code")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QrCodeEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    private boolean valid;

}
