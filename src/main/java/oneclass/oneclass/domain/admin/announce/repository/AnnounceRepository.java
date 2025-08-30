package oneclass.oneclass.domain.admin.announce.repository;

import oneclass.oneclass.domain.admin.announce.entity.Announce;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnounceRepository extends JpaRepository<Announce, Long> {
}
