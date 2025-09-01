package oneclass.oneclass.domain.task.repository;

import oneclass.oneclass.domain.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
