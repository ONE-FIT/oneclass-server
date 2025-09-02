package oneclass.oneclass.counsel.repository;

import oneclass.oneclass.counsel.entity.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
    List<Consultation> findByStatus(String status);
    Optional<Consultation> findByNameAndPhone(String name, String phone);
}