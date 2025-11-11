package oneclass.oneclass.domain.announce.repository;

import oneclass.oneclass.domain.announce.entity.Announce;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnnounceRepository extends JpaRepository<Announce, Long> {
    List<Announce> findByTitle(String title);
    Optional<Announce> findById(Long id);
}