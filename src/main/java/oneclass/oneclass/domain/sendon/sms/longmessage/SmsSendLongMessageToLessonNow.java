package oneclass.oneclass.domain.sendon.sms.longmessage;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import oneclass.oneclass.domain.lesson.entity.Lesson;
import oneclass.oneclass.domain.lesson.error.LessonError;
import oneclass.oneclass.domain.lesson.repository.LessonRepository;
import oneclass.oneclass.domain.member.entity.Member;
import oneclass.oneclass.global.exception.CustomException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SmsSendLongMessageToLessonNow extends AbstractLongMessage {

    private final LessonRepository lessonRepository;

    @Setter
    private Long lessonId;

    @Override
    protected Page<String> findTargets(Pageable pageable) {
        if (lessonId == null) {
            throw new CustomException(LessonError.INVALID_MEMBER_ID_VALUE);
        }

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 반입니다."));

        List<String> phones = lesson.getStudents().stream()
                .map(Member::getPhone)
                .filter(phone -> phone != null && !phone.isEmpty())
                .collect(Collectors.toList());

        // 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), phones.size());
        
        if (start >= phones.size()) {
            return new PageImpl<>(List.of(), pageable, phones.size());
        }

        List<String> pagedPhones = phones.subList(start, end);
        return new PageImpl<>(pagedPhones, pageable, phones.size());
    }

    @Override
    public String getDescription() {
        return "[LMS] 반별 즉시문자 발송";
    }
}
