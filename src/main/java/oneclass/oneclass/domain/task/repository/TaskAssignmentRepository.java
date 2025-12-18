package oneclass.oneclass.domain.task.repository;

import oneclass.oneclass.domain.task.entity.TaskAssignment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {
    Optional<TaskAssignment> findByTaskIdAndStudentId(Long taskId, Long studentId);

    @EntityGraph(attributePaths = {"task"})
    List<TaskAssignment> findAllByStudent_Id(Long studentId);
}
