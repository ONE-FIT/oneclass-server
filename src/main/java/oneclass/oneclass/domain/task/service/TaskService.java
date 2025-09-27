package oneclass.oneclass.domain.task.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.lesson.entity.Lesson;
import oneclass.oneclass.domain.lesson.error.LessonError;
import oneclass.oneclass.domain.lesson.repository.LessonRepository;
import oneclass.oneclass.domain.member.entity.Member;
import oneclass.oneclass.domain.sendon.event.TaskAssignmentSavedEvent;
import oneclass.oneclass.domain.task.dto.request.CreateEachTaskRequest;
import oneclass.oneclass.domain.task.dto.request.CreateTaskRequest;
import oneclass.oneclass.domain.task.dto.request.UpdateTaskRequest;
import oneclass.oneclass.domain.task.dto.response.TaskResponse;
import oneclass.oneclass.domain.task.entity.Task;
import oneclass.oneclass.domain.task.entity.TaskAssignment;
import oneclass.oneclass.domain.task.entity.TaskStatus;
import oneclass.oneclass.domain.task.error.TaskError;
import oneclass.oneclass.domain.task.repository.TaskAssignmentRepository;
import oneclass.oneclass.domain.task.repository.TaskRepository;
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
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public TaskResponse createLessonTask(CreateTaskRequest request, Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new CustomException(LessonError.NOT_FOUND));

        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .dueDate(request.dueDate())
                .teacher(lesson.getTeacher())
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

        List<Long> memberId = lesson.getStudents().stream().map(Member::getId).toList();

        eventPublisher.publishEvent(new TaskAssignmentSavedEvent(request.description(), request.title(), memberId));

        return TaskResponse.of(savedTask);
    }

    @Transactional
    public TaskResponse createEachTask(CreateEachTaskRequest request) {
        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .dueDate(request.dueDate())
                .teacher(request.teacher())
                .build();

        Task savedTask = taskRepository.save(task);

        TaskAssignment assignment = new TaskAssignment();
        assignment.setTask(savedTask);
        assignment.setStudent(request.student());
        assignment.setTaskStatus(TaskStatus.ASSIGNED);

        taskAssignmentRepository.save(assignment);

        Long memberId = request.student().getId();

        eventPublisher.publishEvent(new TaskAssignmentSavedEvent(request.description(), request.title(), List.of(memberId)));

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