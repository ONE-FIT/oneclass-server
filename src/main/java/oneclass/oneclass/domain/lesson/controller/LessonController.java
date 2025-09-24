package oneclass.oneclass.domain.lesson.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.lesson.dto.request.CreateLessonRequest;
import oneclass.oneclass.domain.lesson.dto.request.UpdateLessonRequest;
import oneclass.oneclass.domain.lesson.dto.response.LessonResponse;
import oneclass.oneclass.domain.lesson.service.LessonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lesson")
@CrossOrigin("*")
@Tag(name = "Lesson API", description = "레슨 관련 API")
public class LessonController {
    private final LessonService lessonService;

    @Operation(summary = "만들기", description = "강의를 만들었습니다.")
    @PostMapping("/create")
    public LessonResponse createLesson(@RequestBody CreateLessonRequest request) {
        return lessonService.createLesson(request);
    }

    @GetMapping("/id/{lessonId}")
    public LessonResponse findLessonById(@PathVariable Long lessonId) {
        return lessonService.findLessonById(lessonId);
    }

    @GetMapping("/title/{title}")
    public LessonResponse findLessonByTitle(@PathVariable String title) {
        return lessonService.findLessonByTitle(title);
    }

    @PatchMapping()
    public LessonResponse updateLesson(@RequestBody UpdateLessonRequest request) {
        return lessonService.updateLesson(request);
    }

    @DeleteMapping("/{lessonId}")
    public void deleteLesson(@PathVariable Long lessonId) {
        lessonService.deleteLesson(lessonId);
    }

    @GetMapping("/all")
    public List<LessonResponse> findAllLessons() {return lessonService.findAll();}

    @PostMapping("/{lessonId}/students/{id}")
    public ResponseEntity<String> addStudentToLesson(
            @PathVariable Long lessonId,
            @PathVariable Long id) {
        lessonService.addStudentToLesson(lessonId, id);
        return ResponseEntity.ok("학생이 수업에 등록되었습니다.");
    }
}
