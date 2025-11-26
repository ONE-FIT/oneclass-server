package oneclass.oneclass.domain.academy.dto.request;

public record ApproveAcademyRequest(
        String adminUsername,
        String password,
        String academyCode
) {
}
