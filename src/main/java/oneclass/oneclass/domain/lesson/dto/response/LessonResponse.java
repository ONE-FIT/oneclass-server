package oneclass.oneclass.domain.lesson.dto.response;

import java.util.List;

import oneclass.oneclass.domain.lesson.entity.Lesson;
import oneclass.oneclass.domain.member.entity.Member;

public record LessonResponse(
        Long lessonId,
        String title,
        Long teacherId,
        String teacherName,
        List<Long> studentIds,
        List<String> studentNames
) {
    public static LessonResponse of(Lesson lesson) {
        List<Long> studentIds = lesson.getStudents().stream()
                .map(Member::getId)
                .toList();
        
        List<String> studentNames = lesson.getStudents().stream()
                .map(Member::getName)
                .toList();
        
        return new LessonResponse(
                lesson.getLessonId(),
                lesson.getTitle(),
                lesson.getTeacher().getId(),
                lesson.getTeacher().getName(),
                studentIds,
                studentNames
        );
    }
}
