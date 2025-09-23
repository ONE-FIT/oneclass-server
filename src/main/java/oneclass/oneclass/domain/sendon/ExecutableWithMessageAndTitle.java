package oneclass.oneclass.domain.sendon;

public interface ExecutableWithMessageAndTitle {
    void execute(String message, String title) throws InterruptedException;
}
