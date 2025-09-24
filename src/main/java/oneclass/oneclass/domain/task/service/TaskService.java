package oneclass.oneclass.domain.task.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.lesson.entity.Lesson;
import oneclass.oneclass.domain.lesson.error.LessonError;
import oneclass.oneclass.domain.lesson.repository.LessonRepository;
import oneclass.oneclass.domain.task.dto.request.CreateEachTaskRequest;
import oneclass.oneclass.domain.sendon.sms.event.TaskSavedEvent;
import oneclass.oneclass.domain.task.dto.request.CreateTaskRequest;
import oneclass.oneclass.domain.task.dto.request.UpdateTaskRequest;
import oneclass.oneclass.domain.task.dto.response.TaskResponse;
import oneclass.oneclass.domain.task.entity.Task;
import oneclass.oneclass.domain.task.entity.TaskAssignment;
import oneclass.oneclass.domain.task.entity.TaskStatus;
import oneclass.oneclass.domain.task.error.TaskError;
import oneclass.oneclass.domain.task.repository.TaskAssignmentRepository;
import oneclass.oneclass.domain.task.repository.TaskRepository;
import oneclass.oneclass.global.auth.member.entity.Member;
import oneclass.oneclass.global.auth.member.repository.MemberRepository;
import oneclass.oneclass.global.exception.CustomException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final LessonRepository lessonRepository;

    @Transactional
    public TaskResponse createLessonTask(CreateTaskRequest request, Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new CustomException(LessonError.NOT_FOUND));
//     private final ApplicationEventPublisher eventPublisher;

        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .dueDate(request.dueDate())
                .build();

        Task savedTask = taskRepository.save(task);

        // 학생들한테 바로 할당
        for (Member student : lesson.getStudents()) {
            TaskAssignment assignment = new TaskAssignment();
            assignment.setTask(savedTask);
            assignment.setStudent(student);
            assignment.setTaskStatus(TaskStatus.ASSIGNED);

            taskAssignmentRepository.save(assignment);
        }

        return TaskResponse.of(savedTask);
    }

    public TaskResponse createEachTask(CreateEachTaskRequest request) {
        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .dueDate(request.dueDate())
                .assignedBy(request.assignedBy())
                .build();
        Task savedTask = taskRepository.save(task);
        //eventPublisher.publishEvent(new TaskSavedEvent(request.description(), request.title()));

        return TaskResponse.of(savedTask);
    }

    public TaskResponse findTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new CustomException(TaskError.NOT_FOUND));
        return TaskResponse.of(task);
    }

    public TaskResponse findTaskByTitle(String title) {
        Task task = taskRepository.findByTitle(title)
                .orElseThrow(() -> new CustomException(TaskError.NOT_FOUND));
        return TaskResponse.of(task);
    }

//    public TaskResponse findTaskByStatus(TaskStatus status) {
//        Task task = taskRepository.findByTaskStatus(status)
//                .orElseThrow(() -> new CustomException(TaskError.NOT_FOUND));
//        return TaskResponse.of(task);
//    }


    public TaskResponse updateTask(UpdateTaskRequest request) {
        Task task = taskRepository.findById(request.id())
                .orElseThrow(() -> new CustomException(TaskError.NOT_FOUND));
        task.setDescription(request.description());
        task.setDueDate(request.dueDate());
        taskRepository.save(task);

        return TaskResponse.of(task);
    }

    public void deleteTask(Long id) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() ->new CustomException(TaskError.NOT_FOUND));
        taskRepository.delete(task);
    }

    public List<TaskResponse> findAll() {
        return taskRepository.findAll().stream().map(TaskResponse::of).collect(Collectors.toList());
    }
}