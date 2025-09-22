package oneclass.oneclass.domain.lesson.controller;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.lesson.dto.request.CreateLessonRequest;
import oneclass.oneclass.domain.lesson.dto.request.UpdateLessonRequest;
import oneclass.oneclass.domain.lesson.dto.response.LessonResponse;
import oneclass.oneclass.domain.lesson.service.LessonService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lesson")
@CrossOrigin("*")
public class LessonController {
    private final LessonService lessonService;

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

    @GetMapping("/all")
    public List<LessonResponse> findAllLessons() {return lessonService.findAll();}
}
