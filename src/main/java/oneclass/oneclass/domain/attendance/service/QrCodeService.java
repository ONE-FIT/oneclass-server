package oneclass.oneclass.domain.attendance.service;


import oneclass.oneclass.domain.attendance.dto.response.QrCodeResponse;

import java.util.concurrent.CompletableFuture;

public interface QrCodeService {

    CompletableFuture<QrCodeResponse> generate();

}
