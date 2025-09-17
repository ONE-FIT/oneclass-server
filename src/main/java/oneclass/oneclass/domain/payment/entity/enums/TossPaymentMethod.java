package oneclass.oneclass.domain.payment.entity.enums;

public enum TossPaymentMethod {
    BANK_TRANSFER,  // 계좌이체(퀵계좌이체)
    CARD, // (신용/체크카드)
    TOSS_PAY, // (토스페이)
    PAYCO,
    KAKAO_PAY,
    NAVER_PAY,
    UNKNOWN, MOBILE // 휴대폰
}
