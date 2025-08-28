package oneclass.oneclass.domain.attendence.domain.mapper;

import oneclass.oneclass.domain.attendence.entity.QrCodeEntity;
import oneclass.oneclass.global.annotation.Mapper;

import java.util.UUID;
@Mapper
public class QrCodeMapper {

    public QrCodeEntity createQrCodeEntity(final Long userId) {
        return QrCodeEntity.builder()
                .userId(userId)
                .code(UUID.randomUUID().toString().replace("-", ""))
                .valid(true)
                .build();
    }
}
