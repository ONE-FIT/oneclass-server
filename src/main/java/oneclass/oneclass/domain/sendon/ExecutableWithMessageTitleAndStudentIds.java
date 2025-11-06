package oneclass.oneclass.domain.sendon;

import java.util.List;

public interface ExecutableWithMessageTitleAndStudentIds {
    void execute(String message, String title, List<Long> StudentId) throws InterruptedException;
}