package oneclass.oneclass.domain.payment.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.auth.CustomUserDetails;
import oneclass.oneclass.domain.payment.entity.TossPayment;
import oneclass.oneclass.domain.payment.service.TossPaymentService;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Controller
@RequiredArgsConstructor
public class TossPaymentController {

    private final TossPaymentService paymentService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${toss.secret-key}")
    private String widgetSecretKey;

    @PostMapping("/confirm")
    public ResponseEntity<JSONObject> confirmPayment(@RequestBody String jsonBody) throws Exception {
        JSONParser parser = new JSONParser();
        String orderId;
        String amount;
        String paymentKey;

        try {
            JSONObject requestData = (JSONObject) parser.parse(jsonBody);
            paymentKey = (String) requestData.get("paymentKey");
            orderId = (String) requestData.get("orderId");
            amount = (String) requestData.get("amount");
        } catch (ParseException e) {
            logger.error("클라이언트 요청 파싱 실패", e);
            return ResponseEntity.badRequest().body(errorResponse("잘못된 요청입니다."));
        }

        JSONObject obj = new JSONObject();
        obj.put("orderId", orderId);
        obj.put("amount", amount);
        obj.put("paymentKey", paymentKey);

        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encodedBytes = encoder.encode((widgetSecretKey + ":").getBytes(StandardCharsets.UTF_8));
        String authorizations = "Basic " + new String(encodedBytes);

        URL url = new URL("https://api.tosspayments.com/v1/payments/confirm");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", authorizations);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(obj.toString().getBytes(StandardCharsets.UTF_8));
        }

        int code = connection.getResponseCode();
        boolean isSuccess = code == 200;

        try (InputStream responseStream = isSuccess ? connection.getInputStream() : connection.getErrorStream();
             Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8)) {

            JSONObject jsonObject = (JSONObject) parser.parse(reader);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long studentId = ((CustomUserDetails) authentication.getPrincipal()).getId();

            String method = (String) jsonObject.get("method");
            TossPayment payment = paymentService.updatePaymentAfterConfirm(
                    studentId,
                    orderId,
                    Integer.parseInt(amount),
                    paymentKey,
                    method,
                    isSuccess
            );

            if (isSuccess) {
                logger.info("결제 성공 - orderId: {}, studentId: {}", orderId, studentId);
            } else {
                logger.warn("결제 실패 - orderId: {}, reason: {}", orderId, jsonObject.get("message"));
            }

            return ResponseEntity.status(code).body(jsonObject);
        }
    }

    @GetMapping("/success")
    public String paymentRequest(HttpServletRequest request, Model model) {
        return "/success";
    }

    @GetMapping("/")
    public String index(HttpServletRequest request, Model model) {
        return "/checkout";
    }

    @GetMapping("/fail")
    public String failPayment(HttpServletRequest request, Model model) {
        model.addAttribute("code", request.getParameter("code"));
        model.addAttribute("message", request.getParameter("message"));
        return "/fail";
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JSONObject> handleException(Exception e) {
        logger.error("결제 처리 중 에러 발생", e);
        return ResponseEntity.internalServerError().body(errorResponse("결제 처리 중 오류가 발생했습니다."));
    }

    private JSONObject errorResponse(String message) {
        JSONObject error = new JSONObject();
        error.put("error", true);
        error.put("message", message);
        return error;
    }
}