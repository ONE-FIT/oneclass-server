package oneclass.oneclass.domain.parents.consultation.repository;

import oneclass.oneclass.domain.parents.consultation.entity.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
}
