package oneclass.oneclass.domain.member.dto.request;


import java.util.List;

public record AddStudentsRequest(
        String phone,
        String password,
        List<String> studentPhones
) {
}