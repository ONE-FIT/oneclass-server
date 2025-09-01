package oneclass.oneclass.domain.student.attendance.mapper;

import oneclass.oneclass.domain.student.attendance.entity.QrCodeEntity;
import oneclass.oneclass.domain.student.attendance.annotation.Mapper;

import java.util.UUID;
@Mapper
public class QrCodeMapper {

    public QrCodeEntity createQrCodeEntity(final Long userId) {
        return QrCodeEntity.builder()
                .userId(userId)
                .code(UUID.randomUUID().toString().replace("-", "")) // <-- 얘를 보내서 Qr 만듬
                .valid(true) // <--- 유효한 코드인지 판별하기 위해 있는 부분이고 방금 만든 코드를 써야하니 TRUE로 저장
                .build();
    }
}
