package oneclass.oneclass.domain.payment.entity.enums;

public enum TossPaymentStatus {
    READY,       // 결제 대기
    PENDING,     // 결제 진행 중
    COMPLETED,   // 결제 완료
    FAILED,      // 결제 실패
    CANCELED     // 결제 취소
}
