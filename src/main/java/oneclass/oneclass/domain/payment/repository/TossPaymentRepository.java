package oneclass.oneclass.domain.payment.repository;

import oneclass.oneclass.domain.payment.entity.TossPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TossPaymentRepository extends JpaRepository<TossPayment, Long> {
    Optional<TossPayment> findByOrderId(String orderId);
}
