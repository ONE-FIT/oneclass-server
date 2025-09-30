package oneclass.oneclass.domain.lesson.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.lesson.dto.request.CreateLessonRequest;
import oneclass.oneclass.domain.lesson.dto.request.UpdateLessonRequest;
import oneclass.oneclass.domain.lesson.dto.response.LessonResponse;
import oneclass.oneclass.domain.lesson.entity.Lesson;
import oneclass.oneclass.domain.lesson.error.LessonError;
import oneclass.oneclass.domain.lesson.repository.LessonRepository;
import oneclass.oneclass.domain.member.entity.Member;
import oneclass.oneclass.domain.member.error.MemberError;
import oneclass.oneclass.domain.member.repository.MemberRepository;
import oneclass.oneclass.domain.task.entity.Task;
import oneclass.oneclass.domain.task.entity.TaskAssignment;
import oneclass.oneclass.domain.task.entity.TaskStatus;
import oneclass.oneclass.domain.task.repository.TaskAssignmentRepository;
import oneclass.oneclass.global.exception.CustomException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonService {
    private final LessonRepository lessonRepository;
    private final MemberRepository memberRepository;

    public LessonResponse createLesson(CreateLessonRequest request) {
        Lesson lesson  = Lesson.builder()
                .title(request.title())
                .teacher(request.teacher())
                .build();
        return LessonResponse.of(lessonRepository.save(lesson));
    }

    public LessonResponse findLessonById(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new CustomException(LessonError.NOT_FOUND));

        return LessonResponse.of(lesson);
    }

    public LessonResponse findLessonByTitle(String title) {
        Lesson lesson = lessonRepository.findByTitle(title)
                .orElseThrow(() -> new CustomException(LessonError.NOT_FOUND));

        return LessonResponse.of(lesson);
    }

    public LessonResponse updateLesson(UpdateLessonRequest request) {
        Lesson lesson = lessonRepository.findById(request.lessonId())
                .orElseThrow(() -> new CustomException(LessonError.NOT_FOUND));
        lesson.setTitle(request.title());
        lesson.setTeacher(request.teacher());
        lessonRepository.save(lesson);

        return LessonResponse.of(lesson);
    }

    public void deleteLesson(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new CustomException(LessonError.NOT_FOUND));

        lessonRepository.delete(lesson);
    }

    @Transactional
    public void addStudentToLesson(Long lessonId, Long studentId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new CustomException(LessonError.NOT_FOUND));

        Member student = memberRepository.findById(studentId)
                .orElseThrow(() -> new CustomException(MemberError.NOT_FOUND));

        lesson.getStudents().add(student);
        lessonRepository.save(lesson);
    }

    public List<LessonResponse> findAll() {
        return lessonRepository.findAll().stream().map(LessonResponse::of).collect(Collectors.toList());
    }

}
