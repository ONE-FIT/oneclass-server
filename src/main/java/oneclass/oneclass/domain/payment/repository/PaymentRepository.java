package oneclass.oneclass.domain.payment.repository;

import oneclass.oneclass.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
