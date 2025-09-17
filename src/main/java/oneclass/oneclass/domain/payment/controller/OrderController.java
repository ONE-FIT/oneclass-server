package oneclass.oneclass.domain.payment.controller;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.payment.entity.TossPayment;
import oneclass.oneclass.domain.payment.service.TossPaymentService;
import oneclass.oneclass.global.auth.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final TossPaymentService paymentService;

    @PostMapping("/order/save")
    @ResponseBody
    public ResponseEntity<?> saveOrder(@RequestBody Map<String, Object> data) {
        try {
            String orderId = (String) data.get("orderId");
            Number amountNum = (Number) data.get("amount"); // 안전하게 Number로 받음
            int amount = amountNum.intValue();

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long studentId = ((CustomUserDetails) authentication.getPrincipal()).getId();

            TossPayment payment = paymentService.createPendingPayment(studentId, orderId, amount);

            return ResponseEntity.ok(Map.of("success", true, "orderId", payment.getOrderId()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}