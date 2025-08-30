package oneclass.oneclass.domain.parents.payment.repository;

import oneclass.oneclass.domain.parents.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
