package oneclass.oneclass.domain.member.dto.request;


import java.util.List;

public record TeacherStudentsRequest(
    List<String> studentPhones,
    String password){
    public List<String> getStudentPhones(){
        return studentPhones;
    }
}