package oneclass.oneclass.domain.announce.dto.request;

// CreateAnnounceRequest.java
public record CreateAnnounceRequest(
        String title,
        String content,
        Boolean important,
        Long lessonId // ✅ 추가
) { }