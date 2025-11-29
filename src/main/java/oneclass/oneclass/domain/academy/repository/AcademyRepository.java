package oneclass.oneclass.domain.academy.repository;

import oneclass.oneclass.domain.academy.dto.response.PendingAcademyResponse;
import oneclass.oneclass.domain.academy.entity.Academy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AcademyRepository extends JpaRepository<Academy, String> {
    Optional<Academy> findByAcademyCode(String academyCode);
    Optional<Academy> findByAcademyCodeAndAcademyName(String academyCode, String academyName);
    Optional<Academy> findByEmail(String email);
    Optional<Academy> findByPhone(String phone);

    @Query("""
           select new oneclass.oneclass.domain.academy.dto.response.PendingAcademyResponse(
               a.academyCode, a.academyName, a.email, a.phone, a.status)
           from Academy a
           where a.status = :status
           """)
    List<PendingAcademyResponse> findPendingAcademiesByStatus(@Param("status") Academy.Status status);
}