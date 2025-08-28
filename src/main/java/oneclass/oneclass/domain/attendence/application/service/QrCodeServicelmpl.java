package oneclass.oneclass.domain.attendence.application.service;


import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.attendence.domain.mapper.QrCodeMapper;
import oneclass.oneclass.domain.attendence.domain.repository.jpa.QrCodeRepository;
import oneclass.oneclass.domain.attendence.client.dto.response.QrCodeResponse;
import oneclass.oneclass.domain.attendence.entity.QrCodeEntity;
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
        QrCodeEntity qrCodeEntity = qrRepository
                .save(qrCodeMapper.createQrCodeEntity(1L));
        return CompletableFuture.completedFuture(
                QrCodeResponse.builder()
                        .code(qrCodeEntity.getCode())
                        .build()
        );
    }

}
