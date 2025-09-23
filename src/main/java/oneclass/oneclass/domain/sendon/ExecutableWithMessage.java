package oneclass.oneclass.domain.sendon;

public interface ExecutableWithMessage {
    void execute(String message) throws InterruptedException;
}
