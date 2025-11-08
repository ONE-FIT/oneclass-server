package oneclass.oneclass.domain.academy.dto.request;


public record AcademySignupRequest(
        String academyName,
        String email,
        String password,
        String checkPassword,
        String phone) {
}
