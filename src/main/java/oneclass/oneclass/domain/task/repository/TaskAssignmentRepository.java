package oneclass.oneclass.domain.task.repository;

import oneclass.oneclass.domain.task.entity.TaskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {
}
