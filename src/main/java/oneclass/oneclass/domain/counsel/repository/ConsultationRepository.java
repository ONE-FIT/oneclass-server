package oneclass.oneclass.domain.counsel.repository;

import oneclass.oneclass.domain.counsel.entity.Consultation;
import oneclass.oneclass.domain.counsel.entity.ConsultationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
    List<Consultation> findByNameAndPhone(String name, String phone);
    List<Consultation> findByStatus(ConsultationStatus status);
}