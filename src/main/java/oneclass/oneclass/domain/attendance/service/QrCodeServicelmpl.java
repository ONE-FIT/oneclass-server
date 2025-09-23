package oneclass.oneclass.domain.attendance.service;


import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.attendance.dto.response.QrCodeResponse;
import oneclass.oneclass.domain.attendance.entity.QrCode;
import oneclass.oneclass.domain.attendance.mapper.QrCodeMapper;
import oneclass.oneclass.domain.attendance.repository.jpa.QrCodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;


@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)

public class QrCodeServicelmpl implements QrCodeService {
    private final QrCodeRepository qrRepository;
    private final QrCodeMapper qrCodeMapper;

    @Override
    public CompletableFuture<QrCodeResponse> generate() {
        qrRepository.updateAllInvalidCheckCode(1L);
        QrCode qrCode = qrRepository
                .save(qrCodeMapper.createQrCodeEntity(1L));
        return CompletableFuture.completedFuture(
                QrCodeResponse.builder()
                        .code(qrCode.getCode())
                        .build()
        );
    }

}
