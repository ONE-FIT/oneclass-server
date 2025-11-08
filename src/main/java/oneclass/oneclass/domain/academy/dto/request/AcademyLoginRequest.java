package oneclass.oneclass.domain.academy.dto.request;


public record AcademyLoginRequest (
        String academyCode,
        String academyName,
        String password){
}
