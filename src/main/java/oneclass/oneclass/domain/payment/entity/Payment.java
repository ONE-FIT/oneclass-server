package oneclass.oneclass.domain.payment.entity;

import jakarta.persistence.*;
import lombok.Data;
import oneclass.oneclass.domain.bill.entity.Bill;
import oneclass.oneclass.domain.payment.entity.enums.PaymentMethod;
import oneclass.oneclass.domain.payment.entity.enums.PaymentStatus;
import oneclass.oneclass.global.auth.member.entity.Member;

import java.time.LocalDateTime;

@Entity
@Data
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id")
    private Bill bill;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id")
    private Member payer;

    private int amount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method; // CARD / CASH / BANK_TRANSFER

    private String transactionId; // PG 결제 번호 또는 현금 영수증 번호
    private LocalDateTime paymentDate;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // SUCCESS / FAILED / REFUNDED
}

