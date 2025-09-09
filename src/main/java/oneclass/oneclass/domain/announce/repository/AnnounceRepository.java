package oneclass.oneclass.domain.announce.repository;

import oneclass.oneclass.domain.announce.entity.Announce;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnounceRepository extends JpaRepository<Announce, Long> {

}
