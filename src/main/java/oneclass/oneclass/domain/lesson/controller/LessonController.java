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

    @GetMapping("/id/{lid}")
    public LessonResponse findLessonById(@PathVariable Long lid) {
        return lessonService.findLessonById(lid);
    }

    @GetMapping("/title/{title}")
    public LessonResponse findLessonByTitle(@PathVariable String title) {
        return lessonService.findLessonByTitle(title);
    }

    @PatchMapping()
    public LessonResponse updateLesson(@RequestBody UpdateLessonRequest request) {
        return lessonService.updateLesson(request);
    }

    @DeleteMapping("/{lid}")
    public void deleteLesson(@PathVariable Long lid) {
        lessonService.deleteLesson(lid);
    }

    /**
     * 특정 수업의 모든 학생에게 과제 배정
     */
    @PostMapping("/assign-tasks/{lid}")
    public ResponseEntity<String> assignTasks(@PathVariable Long lid) {
        lessonService.assignLessonTasks(lid);
        return ResponseEntity.ok("과제가 모든 학생에게 배정되었습니다.");
    }

    @GetMapping("/all")
    public List<LessonResponse> findAllLessons() {return lessonService.findAll();}

    @PostMapping("/{lid}/students/{id}")
    public ResponseEntity<String> addStudentToLesson(
            @PathVariable Long lid,
            @PathVariable Long id) {
        lessonService.addStudentToLesson(lid, id);
        return ResponseEntity.ok("학생이 수업에 등록되었습니다.");
    }
}
