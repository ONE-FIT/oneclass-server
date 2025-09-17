package oneclass.oneclass.domain.message;

public interface ExecutableWithMessageAndTitle {
    void execute(String message, String title) throws InterruptedException;
}
