package oneclass.oneclass.domain.student.attendance.service;


import oneclass.oneclass.domain.student.attendance.dto.response.QrCodeResponse;

import java.util.concurrent.CompletableFuture;

public interface QrCodeService {

    CompletableFuture<QrCodeResponse> generate();

}
