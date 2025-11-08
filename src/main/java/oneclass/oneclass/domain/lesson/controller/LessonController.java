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
@Tag(name = "Lesson API", description = "레슨 관련 API")
public class LessonController {
    private final LessonService lessonService;

    @PostMapping("/create")
    @Operation(summary = "생성",
            description = "강의를 만듭니다.")
    public LessonResponse createLesson(@RequestBody CreateLessonRequest request) {
        return lessonService.createLesson(request);
    }

    @GetMapping("/id/{lessonId}")
    @Operation(summary = "검색",
            description = "강의를 id로 찾습니다")
    public LessonResponse findLessonById(@PathVariable Long lessonId) {
        return lessonService.findLessonById(lessonId);
    }

    @GetMapping("/title/{title}")
    @Operation(summary = "검색",
            description = "강의를 title로 검색합니다.")
    public LessonResponse findLessonByTitle(@PathVariable String title) {
        return lessonService.findLessonByTitle(title);
    }

    @PatchMapping()
    @Operation(summary = "수정",
            description = "강의를 수정합니다.")
    public LessonResponse updateLesson(@RequestBody UpdateLessonRequest request) {
        return lessonService.updateLesson(request);
    }

    @DeleteMapping("/{lessonId}")
    @Operation(summary = "강의 삭제",
            description = "강의를 삭제합니다.")
    public void deleteLesson(@PathVariable Long lessonId) {
        lessonService.deleteLesson(lessonId);
    }

    @GetMapping("/all")
    @Operation(summary = "검색",
            description = "모든 강의를 검색합니다.")
    public List<LessonResponse> findAllLessons() {
        return lessonService.findAll();
    }

    @PostMapping("/{lessonId}/students/{id}")
    @Operation(summary = "강의에 학생 추가",
            description = "강의에 학생을 추가합니다.")
    public ResponseEntity<String> addStudentToLesson(
            @PathVariable Long lessonId,
            @PathVariable Long id) {
        lessonService.addStudentToLesson(lessonId, id);
        return ResponseEntity.ok("학생이 수업에 등록되었습니다.");
    }
}
