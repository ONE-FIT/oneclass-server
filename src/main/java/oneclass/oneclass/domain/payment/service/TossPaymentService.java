package oneclass.oneclass.domain.payment.service;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.payment.entity.TossPayment;
import oneclass.oneclass.domain.payment.entity.enums.TossPaymentMethod;
import oneclass.oneclass.domain.payment.entity.enums.TossPaymentStatus;
import oneclass.oneclass.domain.payment.repository.TossPaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TossPaymentService {

    private final TossPaymentRepository tossPaymentRepository;

    // TODO: 학생에 따라 결제 금액을 검증하세요.

    /**
     * 결제 전 주문 저장 (PENDING 상태)
     */
    @Transactional
    public TossPayment createPendingPayment(Long studentId, String orderId, int amount) {
        TossPayment payment = new TossPayment();
        payment.setStudentId(studentId);
        payment.setOrderId(orderId);
        payment.setAmount(amount);
        payment.setTossPaymentStatus(TossPaymentStatus.PENDING);
        payment.setRequestedAt(LocalDateTime.now());
        return tossPaymentRepository.save(payment);
    }

    /**
     * 결제 승인 후 결제 정보 업데이트
     */
    @Transactional
    public TossPayment updatePaymentAfterConfirm(Long studentId, String orderId, int amount, String paymentKey, String method, boolean success) {
        TossPayment payment = tossPaymentRepository.findByOrderId(orderId).orElseThrow(() -> new IllegalArgumentException("잘못된 orderId"));

        payment.setAmount(amount);
        payment.setPaymentKey(paymentKey);

        if (method != null) {
            try {
                payment.setTossPaymentMethod(TossPaymentMethod.valueOf(method.toUpperCase()));
            } catch (IllegalArgumentException e) {
                payment.setTossPaymentMethod(TossPaymentMethod.UNKNOWN);
            }
        } else {
            payment.setTossPaymentMethod(TossPaymentMethod.UNKNOWN);
        }

        if (success) {
            payment.setTossPaymentStatus(TossPaymentStatus.COMPLETED);
            payment.setCompletedAt(LocalDateTime.now());
        } else {
            payment.setTossPaymentStatus(TossPaymentStatus.FAILED);
        }

        return tossPaymentRepository.save(payment);
    }
}