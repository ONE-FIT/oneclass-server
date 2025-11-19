package oneclass.oneclass.domain.announce.dto.request;

public record CreateAnnounceRequest(
        String title,
        String content,
        Boolean important,
        String reservation,
        Long lessonId // ✅ 추가
) {
}