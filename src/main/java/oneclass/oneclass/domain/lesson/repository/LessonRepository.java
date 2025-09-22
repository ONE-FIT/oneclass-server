package oneclass.oneclass.domain.lesson.repository;

import oneclass.oneclass.domain.lesson.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    Optional<Lesson> findByTitle(String title);
    Optional<Lesson> findById(Long Lid);
}
