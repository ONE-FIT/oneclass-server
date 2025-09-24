package oneclass.oneclass.domain.counsel.repository;

import oneclass.oneclass.domain.counsel.entity.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
    Optional<Consultation> findByNameAndPhone(String name, String phone);
}