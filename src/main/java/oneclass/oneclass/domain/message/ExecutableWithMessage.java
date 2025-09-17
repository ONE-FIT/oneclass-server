package oneclass.oneclass.domain.message;

public interface ExecutableWithMessage {
    void execute(String message) throws InterruptedException;
}
