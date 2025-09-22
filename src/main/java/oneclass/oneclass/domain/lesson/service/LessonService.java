package oneclass.oneclass.domain.lesson.service;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.lesson.dto.request.CreateLessonRequest;
import oneclass.oneclass.domain.lesson.dto.request.UpdateLessonRequest;
import oneclass.oneclass.domain.lesson.dto.response.LessonResponse;
import oneclass.oneclass.domain.lesson.entity.Lesson;
import oneclass.oneclass.domain.lesson.error.LessonError;
import oneclass.oneclass.domain.lesson.repository.LessonRepository;
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
