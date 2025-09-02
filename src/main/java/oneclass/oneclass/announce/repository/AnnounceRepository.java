package oneclass.oneclass.announce.repository;

import oneclass.oneclass.announce.entity.Announce;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnounceRepository extends JpaRepository<Announce, Long> {

}
