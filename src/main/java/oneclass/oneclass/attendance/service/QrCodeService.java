package oneclass.oneclass.attendance.service;


import oneclass.oneclass.attendance.dto.response.QrCodeResponse;

import java.util.concurrent.CompletableFuture;

public interface QrCodeService {

    CompletableFuture<QrCodeResponse> generate();

}
