package oneclass.oneclass.domain.admin.task.repository;

import oneclass.oneclass.domain.admin.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
