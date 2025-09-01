package oneclass.oneclass.domain.consultation.repository;

import oneclass.oneclass.domain.consultation.entity.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
}
