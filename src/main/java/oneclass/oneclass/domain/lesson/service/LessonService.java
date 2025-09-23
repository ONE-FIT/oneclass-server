package oneclass.oneclass.domain.lesson.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.lesson.dto.request.CreateLessonRequest;
import oneclass.oneclass.domain.lesson.dto.request.UpdateLessonRequest;
import oneclass.oneclass.domain.lesson.dto.response.LessonResponse;
import oneclass.oneclass.domain.lesson.entity.Lesson;
import oneclass.oneclass.domain.lesson.error.LessonError;
import oneclass.oneclass.domain.lesson.repository.LessonRepository;
import oneclass.oneclass.domain.task.entity.Task;
import oneclass.oneclass.domain.task.entity.TaskAssignment;
import oneclass.oneclass.domain.task.entity.TaskStatus;
import oneclass.oneclass.domain.task.repository.TaskAssignmentRepository;
import oneclass.oneclass.domain.task.repository.TaskRepository;
import oneclass.oneclass.global.auth.member.entity.Member;
import oneclass.oneclass.global.auth.member.repository.MemberRepository;
import oneclass.oneclass.global.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Service
@RequiredArgsConstructor
public class LessonService {
    private final LessonRepository lessonRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;

    @Transactional
    public void assignLessonTasks(Long lid) {
        // 1. lessonId로 Lesson 조회
        Lesson lesson = lessonRepository.findById(lid)
                .orElseThrow(() -> new CustomException(LessonError.NOT_FOUND));

        // 2. 연관된 학생과 과제 가져오기
        List<Member> students = lesson.getStudents();  // Lesson 엔티티에 @OneToMany(or @ManyToMany) 매핑 필요
        List<Task> tasks = lesson.getTasks();          // Lesson 엔티티에 @OneToMany 매핑 필요

        // 3. 모든 (학생 × Task) 조합으로 TaskAssignment 생성
        for (Task task : tasks) {
            for (Member student : students) {
                TaskAssignment assignment = new TaskAssignment();
                assignment.setTask(task);
                assignment.setStudent(student);
                assignment.setTaskStatus(TaskStatus.ASSIGNED);

                taskAssignmentRepository.save(assignment);
            }
        }
    }

    public LessonResponse createLesson(CreateLessonRequest request) {
        Lesson lesson  = Lesson.builder()
                .title(request.title())
                .teacher(request.teacher())
                .build();
        return LessonResponse.of(lessonRepository.save(lesson));
    }

    public LessonResponse findLessonById(Long Lid) {
        Lesson lesson = lessonRepository.findById(Lid)
                .orElseThrow(() -> new CustomException(LessonError.NOT_FOUND));

        return LessonResponse.of(lesson);
    }

    public LessonResponse findLessonByTitle(String title) {
        Lesson lesson = lessonRepository.findByTitle(title)
                .orElseThrow(() -> new CustomException(LessonError.NOT_FOUND));

        return LessonResponse.of(lesson);
    }

    public LessonResponse updateLesson(UpdateLessonRequest request) {
        Lesson lesson = lessonRepository.findById(request.lid())
                .orElseThrow(() -> new CustomException(LessonError.NOT_FOUND));
        lesson.setTitle(request.title());
        lesson.setTeacher(request.teacher());
        lessonRepository.save(lesson);

        return LessonResponse.of(lesson);
    }

    public void deleteLesson(Long Lid) {
        Lesson lesson = lessonRepository.findById(Lid)
                .orElseThrow(() -> new CustomException(LessonError.NOT_FOUND));

        lessonRepository.delete(lesson);
    }

    public List<LessonResponse> findAll() {
        return lessonRepository.findAll().stream().map(LessonResponse::of).collect(Collectors.toList());
    }

}
