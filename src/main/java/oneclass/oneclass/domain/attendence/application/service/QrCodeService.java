package oneclass.oneclass.domain.attendence.application.service;


import oneclass.oneclass.domain.attendence.client.dto.response.QrCodeResponse;

import java.util.concurrent.CompletableFuture;

public interface QrCodeService {

    CompletableFuture<QrCodeResponse> generate();

}
