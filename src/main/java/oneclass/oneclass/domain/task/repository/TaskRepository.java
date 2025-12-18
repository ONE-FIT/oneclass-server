package oneclass.oneclass.domain.task.repository;

import oneclass.oneclass.domain.task.entity.Task;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByTitle(String title);


    @EntityGraph(attributePaths = {"lesson", "teacher"})
    List<Task> findAllByLesson_Teacher_Id(Long teacherId);
}